package Scanner;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
/*
 * author: Huaqing Liu
 * 
 *(1) program   :  blockst '.'
 *(2) statmt     :  decl  | assstat | ifstat | blockst | loopst  | iostat | <empty>
 *(3) decl       :  BASICTYPTOK IDTOK 
 *(4) assstat    :  idref   ASTOK  expression 
 *(5) ifstat     :  IFTOK exp THENTOK  statmt  
 *(6) loopst     :  WHILETOK exp DOTOK statmt 
 *(7) blockst    :  BEGINTOK  {statmt ';'} ENDTOK
 *(8) iostat	 :  READTOK ( idref ) | WRITETOK (expression)
 *(9) expression :  term { ADDOPTOK term }
 *(10) term      :  relfactor {MULOPTOK relfactor}
 *(11) relfact   :  factor  [ RELOPTOK factor ]
 *(12) factor    :  idref  | LITOK  | NOTTOK factor | ‘(‘ exp ‘)‘
 *(13) idref     :  IDTOK
 *
 *
 *Basic idea:
 *follow recursive descent to implement BNF
 *I am using a global variable to pass between each step.
 *
 * for further coding, any new rule being added just need more function to implement it 
 * 
 *known bugs: none
 *
 *Unemployment instruction:
 * 1. comment
 * 2. Read string
 * 3. declaration string
 * 4. Assignment string
 * 5. Real type
 */

public class Parser{
	private Token tkNum; //relay baton, pass between each function
	private Token LHS;  //LHS, to help assignment
	private int offset; //to count the offset
	private Lex l;
	private SymTable st;
	private BufferedWriter bw;
	private CodeGen cg;
	
	public Parser(FileReader fr, FileWriter fw)
	{
		this.offset = 0;
		this.LHS = new Token(" ", 0);
		l = new Lex(fr);
		st = new SymTable();
		bw = new BufferedWriter(fw);
		cg = new CodeGen();
	}
	
	public Token match(int num, int expect)
	{
		Token tmp = null;
		if(num == expect)
		{
			tmp = l.getToken();
			tkNum = tmp;
		}
		else
		{
			System.out.println("Unexpected Token");
			System.exit(1);
		}
		return tmp;
	}
	//program : blockst '.'
	public void program()
	{
		System.out.println("=>" + "program : blockst '.'");
		try {// file header
			bw.write("#Prolog\n");
			bw.write("\t.data#\n");
			bw.write("ProgStart:	.asciiz	\"Program Start\\n\"\n");
			bw.write("ProgEnd:	.asciiz \"\\nProgram End\\n\"\n");
			bw.write("NEWLINE:  .asciiz \"\\n\"\n");
			bw.write("TRUE: .asciiz \"TRUE\"\n");
			bw.write("FALSE: .asciiz \"FALSE\"\n");
			bw.write("\t.code\n");
			bw.write("\t.globl main\n");
			bw.write("main:\n");
			bw.write("\tmove $fp,$sp\n");
			bw.write("\tla $a0,ProgStart\n");
			bw.write("\tsyscall $print_string\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		tkNum = blockst();
		if(tkNum.getTkNum() == 18) //dot
		{
			match(tkNum.getTkNum(),18);
			try {//file tail
				bw.write("\tla $a0,ProgEnd\n");
				bw.write("\tsyscall $print_string\n");
				bw.write("\tsyscall   $exit\n");
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
			System.out.println("miss END TOKEN");
	}
	//blockst  :  BEGINTOK  {statmt ';'} ENDTOK 
	public Token blockst()
	{
		System.out.println("=>" + "blockst  :  BEGINTOK  {statmt ';'} ENDTOK ");
		if(tkNum.getTkNum() == 7) //begin
		{
			match(tkNum.getTkNum(),7);
			st.setTL(st.createNode());
		}
		else
			System.out.println("Miss BEGIN TOKEN");
		while(tkNum.getTkNum() != 8 && tkNum.getTkNum() != 18)   //8 is END   18 is dot
		{
			tkNum = statemt();
			if(tkNum.getTkNum() == 16) //semi colon
				match(tkNum.getTkNum(), 16);
		}
		if(tkNum.getTkNum() == 8)
		{
			match(tkNum.getTkNum(),8);
			st.backNode();
		}
		return tkNum;
	}
	
	//statmt     :  decl  | assstat | ifstat | blockst | loopst  | iostat | <empty>
	public Token statemt()
	{
		switch(tkNum.getTkNum())
		{
		case 3:  // type
			System.out.print("=>" + "statemt : decl");
			tkNum = decl();
			break;
		case 1: //id
			System.out.print("=>" + "statemt : assstat");
			tkNum = assstat();
			break;
		case 9: //if
			System.out.print("=>" + "statemt : ifstat");
			tkNum = ifstat();
			break;
		case 7: //begin
			System.out.print("=>" + "statemt : blockst");
			tkNum = blockst();
			break;
		case 11: //while
			System.out.print("=>" + "statemt : loopst");
			tkNum = loopst();
			break;
		case 13: //read or write
			System.out.print("=>" + "statemt : iostate");
			tkNum = iostat();
			break;
		default:
			System.out.println("\nUnrecognized Token at " + l.getB().getLineNo());
			System.exit(1);
			break;
		};
		return tkNum;
	}
	//decl :  BASICTYPTOK IDTOK 
	public Token decl()
	{
		System.out.print("=>" + "decl :  BASICTYPTOK IDTOK ");
		String id = " ";
		if(tkNum.getTkNum() == 3) //type tok
		{
			id = match(tkNum.getTkNum(),3).getName();
			if(st.findCurrScope(id).offset == -1)
			{
				st.insert(id,l.getT().getray(1).getName(),this.offset);
				offset-=4;
				try {//the int value will give a default initial value 0
					bw.write("\tmove  $t0,$0\n");
					bw.write("\tsw  $t0," + st.find(id).offset+"($fp)\n");
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
			else
				System.out.println("\nre-defined identifier at " +l.getB().getLineNo());
		}
		if(tkNum.getTkNum() == 1) //id tok
			match(tkNum.getTkNum(),1).getName();
		return tkNum;
	}
	//assstat  :  idref   ASTOK  expression 
	public Token assstat()
	{
		System.out.print("=>" + "assstat  :  idref   ASTOK  expression");
		if(tkNum.getTkNum() == 1) //identifier tok
		{
			LHS = tkNum;
			if(st.find(tkNum.getName()).offset!= -1)
				tkNum = idref();
			else
				System.out.println("undeclare identifier");
		}
		if(tkNum.getTkNum() == 19) // assign tok
			match(tkNum.getTkNum(),19);
		if(!st.typeComparasion(LHS.getName(), tkNum.getName()))
		{
			System.out.println("\ntype is not match to LHS at " + l.getB().getLineNo());
		}
		tkNum = expression();
		try {
			bw.write("\tlw  $t0,"+offset+"($fp)\n");
			bw.write("\tsw  $t0,"+st.find(LHS.getName()).offset+"($fp)\n");
			offset-=4;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tkNum;
	}
	//ifstat  :  IFTOK exp THENTOK  statmt  
	public Token ifstat()
	{
		System.out.print("=>" + "ifstat  :  IFTOK exp THENTOK  statmt");
		String iflable = cg.getIf();
		if(tkNum.getTkNum() == 9) //IF
			match(tkNum.getTkNum(),9);
		tkNum = expression();
		try {
			bw.write("\tbeq $t0,$0,"+iflable+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(tkNum.getTkNum() == 10)//THEN
			match(tkNum.getTkNum(), 10);
		tkNum = statemt();
		try {
			bw.write(iflable+":\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("==>" + tkNum);
		return tkNum;
	}
	//loopst  :  WHILETOK exp DOTOK statmt
	public Token loopst()
	{
		System.out.print("=>" + "loopst  :  WHILETOK exp DOTOK statmt");
		String whileLable = cg.getWhile();
		String endWhile = cg.getEndWhile();
		if(tkNum.getTkNum() == 11)  //WHILE 
			match(tkNum.getTkNum(),11);
		try {
			bw.write(whileLable+":\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		tkNum = expression();
		try {
			bw.write("\tbeq  $t0,$0,"+endWhile+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(tkNum.getTkNum() == 12) //DO
			match(tkNum.getTkNum(),12);
		tkNum = statemt();
		try {
			bw.write("\tb  "+whileLable+"\n");
			bw.write(endWhile+":\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tkNum;
	}
	//iostat	:  READTOK ( idref ) | WRITETOK (expression)
	public Token iostat()
	{
		if(tkNum.getTkNum() == 13) //READ and WRITE
			match(tkNum.getTkNum(),13);
		if(tkNum.getTkNum() == 14) // ( tok
		{
			match(tkNum.getTkNum(),14);
			if(l.getT().getray(2).getName().equals("READ"))
			{

				System.out.print("=>" + "iostat	:  READTOK ( idref )");
				tkNum = idref();
				try {
					bw.write("\tsyscall  $read_int\n");
					bw.write("\tsw  $v0,"+st.find(l.getT().getray(1).getName()).offset+"($fp)\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else
			{
				System.out.print("=>" + "iostat	:  WRITETOK (expression)");
				boolean newline = false;
				if(l.getT().getray(2).getName().equals("WRITELN"))
					newline = true;
				tkNum = expression();
				try {
					if(l.getT().getray(1).getTkNum()==1)
					{
						bw.write("\tmove $a0,$t0\n");
						bw.write("\tsyscall $print_int\n");
					} 
					else
					{
						bw.write("\tsyscall $print_string\n");
					}
					if(newline)
					{
						bw.write("\tla  $a0,NEWLINE\n");
						bw.write("\tsyscall $print_string\n");
					}
					
				} catch (NumberFormatException | IOException e) {
					e.printStackTrace();
				}
			}
			if(tkNum.getTkNum() == 15) // ) tok
				match(tkNum.getTkNum(),15);
		}
		return tkNum;
	}
	//expression  :  term { ADDOPTOK term }
	public Token expression()
	{
		System.out.print("=>" + "expression  :  term { ADDOPTOK term }");
		tkNum = term();
		while(tkNum.getTkNum() == 4) //addTok
		{
			if(tkNum.getTkNum() == 4)
				match(tkNum.getTkNum(),4);
			tkNum = term();
		}
		return tkNum;
	}
	//term   :  relfactor {MULOPTOK relfactor}
	public Token term()
	{
		System.out.print("=>" + " term   :  relfactor {MULOPTOK relfactor}");
		tkNum = relfact();
		while(tkNum.getTkNum() == 5) //mulTok
		{
			if(tkNum.getTkNum() == 5)  
				match(tkNum.getTkNum(),5);
			tkNum = relfact();
		}
		return tkNum;
	}
	//relfact	:  factor  [ RELOPTOK factor ]
	public Token relfact()
	{
		System.out.print("=>" + "relfact  :  factor  [ RELOPTOK factor ]");
		tkNum = factor();
		try {
			bw.write("\tmove  $t1,$t0\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(tkNum.getTkNum() == 6) //relTok
		{

			match(tkNum.getTkNum(),6); //relTOk
			tkNum = factor();
			try {
				bw.write("\tmove  $t2,$t0\n");
				if(l.getT().getray(2).getName().equals("<"))
					bw.write("\tslt  $t0,$t1,$t2\n");
				if(l.getT().getray(2).getName().equals(">"))
					bw.write("\tslt  $t0,$t2,$t1\n");
				if(l.getT().getray(2).getName().equals("="))
					bw.write("\tseq  $t0,$t1,$t2\n");
				if(l.getT().getray(2).getName().equals("!="))
					bw.write("\tsne  $t0,$t1,$t2\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return tkNum;
	}
	//factor  	:  idref  | LITOK  | NOTTOK factor | ‘(‘ exp ‘)‘
	public Token factor()
	{

		switch(tkNum.getTkNum())
		{
		case 1: //Id token
			Token t = null;
			Token li = null;
			System.out.print("\n=>" + "factor  :  idref");
			li = l.getT().getray(2); // to store last identifier
			tkNum = idref();
			if((t = l.getT().getray(2)).getTkNum()==4 || (t = l.getT().getray(2)).getTkNum()==5) //addTok and multok
			{
				if(t.getName().equals("+"))
				{
					try {
						bw.write("\tlw  $t1,"+st.find(li.getName()).offset+"($fp)\n");
						bw.write("\tadd  $t0,$t0,$t1\n");
						offset-=4;
						bw.write("\tsw  $t0,"+offset+"($fp)\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(t.getName().equals("-"))
				{
					try {
						bw.write("\tlw  $t1,"+st.find(li.getName()).offset+"($fp)\n");
						bw.write("\tsub  $t0,$t1,$t0\n");
						offset-=4;
						bw.write("\tsw  $t0,"+offset+"($fp)\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(t.getName().equals("*"))
				{
					try {
						bw.write("\tlw  $t1,"+st.find(li.getName()).offset+"($fp)\n");
						bw.write("\tmul  $t0,$t0,$t1\n");
						offset-=4;
						bw.write("\tsw  $t0,"+offset+"($fp)\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(t.getName().equals("/"))
				{
					try {
						bw.write("\tlw  $t1,"+st.find(li.getName()).offset+"($fp)\n");
						bw.write("\tdiv  $t0,$t1,$t0\n");
						offset-=4;
						bw.write("\tsw  $t0,"+offset+"($fp)\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(t.getName().equals("REM"))
				{
					try {
						bw.write("\tlw  $t1,"+st.find(li.getName()).offset+"($fp)\n");
						bw.write("\trem  $t0,$t1,$t0\n");
						offset-=4;
						bw.write("\tsw  $t0,"+offset+"($fp)\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(t.getName().equals("OR"))
				{
					try {
						bw.write("\tlw  $t1,"+st.find(li.getName()).offset+"($fp)\n");
						bw.write("\tor  $t0,$t0,$t1\n");
						offset-=4;
						bw.write("\tsw  $t0,"+offset+"($fp)\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(t.getName().equals("AND"))
				{
					try {
						bw.write("\tlw  $t1,"+st.find(li.getName()).offset+"($fp)\n");
						bw.write("\tand  $t0,$t0,$t1\n");
						offset-=4;
						bw.write("\tsw  $t0,"+offset+"($fp)\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(t.getName().equals("DIV"))
				{
					try {
						bw.write("\tlw  $t1,"+st.find(li.getName()).offset+"($fp)\n");
						bw.write("\tdiv  $t0,$t1,$t0\n");
						offset-=4;
						bw.write("\tsw  $t0,"+offset+"($fp)\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			break;  
		case 2: //lit tok
			System.out.print("=>" + "factor  : LITOK");
			if((t = l.getT().getray(1)).getTkNum()==4 || (t = l.getT().getray(1)).getTkNum()==5) //addTok and multok
			{
				if(t.getName().equals("+"))
				{
					try {
						bw.write("\tli  $t1,"+Integer.valueOf(tkNum.getName())+"\n");
						if(l.getT().getray(2).getTkNum() == 1)
							bw.write("\tadd  $t0,$t0,$t1\n");
						else
						{
							bw.write("\tlw  $t0,"+offset+"($fp)\n");
							bw.write("\tadd  $t0,$t0,$t1\n");
						}
						offset-=4;
						bw.write("\tsw  $t0,"+ offset +"($fp)\n");
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
				}
				if(t.getName().equals("-"))
				{
					try {
							bw.write("\tlw  $t1,"+offset+"($fp)\n");
							bw.write("\tli  $t2,"+Integer.valueOf(tkNum.getName())+"\n");
							if(l.getT().getray(2).getTkNum() == 1)
								bw.write("\tsub  $t1,$t0,$t2\n");
							else
								bw.write("\tsub  $t1,$t2,$t1\n");
							offset-=4;
							bw.write("\tsw  $t1,"+ offset +"($fp)\n");
						} catch (NumberFormatException | IOException e) {
							e.printStackTrace();
						}
				}
				if(t.getName().equals("OR"))
				{
					try {

						bw.write("\tlw  $t1,"+offset+"($fp)\n");
						bw.write("\tli  $t2,"+Integer.valueOf(tkNum.getName())+"\n");
						if(l.getT().getray(2).getTkNum() == 1)
							bw.write("\tor  $t1,$t0,$t2\n");
						else
							bw.write("\tor  $t1,$t2,$t1\n");
						offset-=4;
						bw.write("\tsw  $t1,"+ offset +"($fp)\n");
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
				}
				if(t.getName().equals("*"))
				{
					try {
						bw.write("\tli  $t0,"+Integer.valueOf(tkNum.getName())+"\n");
						bw.write("\tlw  $t1,"+offset+"($fp)\n");
						bw.write("\tmul  $t0,$t1,$t0\n");
						offset-=4;
						bw.write("\tsw  $t0,"+ offset +"($fp)\n");
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
				}
				if(t.getName().equals("/"))
				{
					try {
						bw.write("\tli  $t0,"+Integer.valueOf(tkNum.getName())+"\n");
						bw.write("\tlw  $t1,"+offset+"($fp)\n");
						bw.write("\tdiv  $t0,$t1,$t0\n");
						offset-=4;
						bw.write("\tsw  $t0,"+ offset +"($fp)\n");
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
				}
				if(t.getName().equals("DIV"))
				{
					try {

						bw.write("\tlw  $t1,"+offset+"($fp)\n");
						bw.write("\tli  $t2,"+Integer.valueOf(tkNum.getName())+"\n");
						if(l.getT().getray(2).getTkNum() == 1)
							bw.write("\tdiv  $t1,$t0,$t2\n");
						else
							bw.write("\tdiv  $t1,$t2,$t1\n");
						offset-=4;
						bw.write("\tsw  $t1,"+ offset +"($fp)\n");
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
				}
				if(t.getName().equals("REM"))
				{
					System.out.println("\n"+t.getName());
					try {

						bw.write("\tlw  $t1,"+offset+"($fp)\n");
						bw.write("\tli  $t2,"+Integer.valueOf(tkNum.getName())+"\n");
						if(l.getT().getray(2).getTkNum() == 1)
							bw.write("\trem  $t1,$t0,$t2\n");
						else
							bw.write("\trem  $t1,$t2,$t1\n");
						offset-=4;
						bw.write("\tsw  $t1,"+ offset +"($fp)\n");
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
				}
				if(t.getName().equals("AND"))
				{
					try {

						bw.write("\tlw  $t1,"+offset+"($fp)\n");
						bw.write("\tli  $t2,"+Integer.valueOf(tkNum.getName())+"\n");
						if(l.getT().getray(2).getTkNum() == 1)
							bw.write("\tand  $t1,$t0,$t2\n");
						else
							bw.write("\tand  $t1,$t2,$t1\n");
						offset-=4;
						bw.write("\tsw  $t1,"+ offset +"($fp)\n");
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
				}
				
			}
			else
			{
				try {
					if(tkNum.getName().charAt(0)=='"')
					{
						String str = cg.getWrite();
						bw.write("\t.data\n");
						bw.write(str+":\t.asciiz\t" + tkNum.getName()+"\n");
						bw.write("\t.code\n");
						bw.write("\tla  $a0,"+str+"\n");
					}
					else if(tkNum.getName().equals("TRUE"))
					{
						bw.write("\tli  $t0,1\n");
						bw.write("\tsw  $t0,"+offset+"($fp)\n");
					}
					else if(tkNum.getName().equals("FALSE"))
					{
						bw.write("\tli  $t0,0\n");
						bw.write("\tsw  $t0,"+offset+"($fp)\n");
					}
					else
					{
						bw.write("\tli  $t0,"+Integer.valueOf(tkNum.getName())+"\n");
						bw.write("\tsw  $t0,"+offset+"($fp)\n");
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
			match(tkNum.getTkNum(),2);
			break;
		case 17: //boolean not
			System.out.print("=>" + "factor : NOTTOK factor ");
			match(tkNum.getTkNum(),17);
			tkNum = factor();
			try {
				bw.write("\tli $t1,1\n");
				bw.write("\txor $t0,$t0,$t1\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 14: // ( tok
			System.out.print("=>" + "factor :‘(‘ exp ‘)‘");
			match(tkNum.getTkNum(),14); // ( token
			tkNum = expression();
			match(tkNum.getTkNum(),15); // ) token
			break;
		default:
			System.out.println("Unexpected Token at "+l.getB().getLineNo());
			break;
		}
		return tkNum;
	}
	//idref   :  IDTOK
	public Token idref()
	{
		System.out.print("=>" + "idref :  IDTOK");
		if(tkNum.getTkNum() == 1)
		{
			if(st.find(tkNum.getName()).offset == -1)
				System.out.println("\nUndeclera variable at " + l.getB().getLineNo());
			else
			{
				try {
					bw.write("\tlw  $t0,"+st.find(tkNum.getName()).offset+"($fp)\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			match(tkNum.getTkNum(),1);
		}
		return tkNum;
	}
	
	public void run()// test function
	{
		tkNum = l.getToken();
		program();
		System.out.println("\nSymbal table:");
		st.print();
	}
}
