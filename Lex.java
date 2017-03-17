package Scanner;
import java.io.FileReader;
import java.util.ArrayList;
//@Author: Huaqing Liu
//identify token
/*TOKEN LIST:
 * 1. identifier
 * 2. literal
 * 3. types
 * 4. addition operator
 * 5. multiplication operator
 * 6. relational operator
 * 7. Begin token
 * 8. END token
 * 9. IF token
 * 10. THEN token
 * 11. WHILE token
 * 12. DO token
 * 13. READ, WRITE AND WRITELN token
 * 14. ( token
 * 15. ) token
 * 16. ; token
 * 17. ! boolean not token
 * 18. . dot end token
 * 19. := token
 * 
 * Basic idea:
 * My idea is that whitespace(include newline , return, tab, etc.) and punctuation is the separator of letter,digits and itself; letter is separator of
 * digits and punctuation, vice versa.
 * for further coding, any new punctuation will need to be consider as separator, and separated by letters.
 * Keywords are maintained in Token class, any new keyword need to be added in kwhandler and isKeyword.
 * Punctuation are maintained in TOken class, too. any new punctuation need to be added in puncHandler and isPunctuations
 * 
 * List of bugs: null
 * uninplementd instruction: 
 * 1. Comment
 * 
 * 
 */
public class Lex{
	private ArrayList<Character> buff = new ArrayList<Character>();
	private Buffer buffer;
	private Token token;
	private char next;
	private char curr;
	private ArrayList<Error> err = new ArrayList<Error>();
	//this enum type for DFA state machine
	private enum State
	{
		quot,str,id,lit,dig,lp,rp,seco,boolnot,dot,equ,error,start,ws,inquot;  //be honest, not all of this be used because I have change code a lot
	}
	private State currState; // for state machine
	//this will convert ArrayList into a string
	private String toString(ArrayList<Character> c)
	{
	    StringBuilder builder = new StringBuilder(c.size());
	    for(Character ch: c)
	    {
	        builder.append(ch);
	    }
	    return builder.toString();
	}
	
	//Constructor
	//to initialize variable
	public Lex(FileReader fr)
	{
		this.buffer = new Buffer(fr);
		this.token = new Token();
		this.next = ' ';
		this.curr = ' ';
		this.currState = State.str;
	}
	//input is one char at time from buffer
	//output will be a TOKEN type which is in (token, token number) 
	//As I understand, punctuation and white space(include newline and tab) is the terminator of Letter and Digits, vice versa
	//this is how the getTOken works, if Letter or Digits are followed by punctuation or whitespace, it will end and return a proper token number and token. vice versa
	public Token getToken()
	{
		//System.out.println(currState);
		boolean run = true;
		while(run)
		{
			next = buffer.getChar();
			curr = buffer.getBackUp();
			switch(currState)
			{
			case str:
				if(Character.isWhitespace(curr))
					currState = State.str;
				else
				{
					if(Character.isLetter(curr) || Character.isDigit(curr))
						buff.add(curr);
					if(Character.isWhitespace(next) || next == '\n' || token.isPunctuation(next))
					{
						run = false;
						if(token.isKeywords(toString(buff)))
						{
							token.setTk(toString(buff),token.kwHandler(toString(buff)));
							if(token.kwHandler(toString(buff)) == 4 || token.kwHandler(toString(buff)) == 5 || token.kwHandler(toString(buff)) == 6) // add token mul token
							{
								buff.add(curr);
								currState = State.lit;
							}
						}
						else if(token.getray(1).getTkNum() == 3)  //Type token
							token.setTk(toString(buff),1);	
						else
							token.setTk(toString(buff),1); // id token
						if(next == ')')
							currState = State.rp;
						buff.clear();
					}//if
					if(token.isPunctuation(curr))
					{
						buff.add(curr);
						if(curr == ';')
						{
							token.setTk(toString(buff),token.puncHandler(curr));
							buff.clear();
							run = false;
						}
						else if(curr == ':' && next =='=')
						{
							token.setTk(":=",19);  // equ token
							currState = State.lit;
							run = false;
							buff.clear();
							buffer.getChar();
						}
						else if(curr == '(')
						{
							token.setTk(toString(buff),token.puncHandler(curr));
							buff.clear();
							currState = State.lp;
							run = false;
						}
						else if(curr == '.')
						{
							token.setTk(toString(buff),token.puncHandler(curr));
							buff.clear();
							run = false;
						}
						else if(curr == '!')
						{
							if(next == '=')
							{
								token.setTk("!=",6);  // mul token
								currState = State.lit;
								buffer.getChar();
							}
							if(Character.isLetter(next))
							{
								token.setTk("!",token.puncHandler(curr)); //boolean not
								currState = State.str;
							}
							run = false;
							buff.clear();
						}
						else if(token.puncHandler(curr)==4 || token.puncHandler(curr)==5 || token.puncHandler(curr)==6) // add token rel token and mul token
						{
							token.setTk(toString(buff),token.puncHandler(curr));
							buff.clear();
							currState = State.lit;
							run = false;
						}
						else if(curr == '=')
						{
							token.setTk(toString(buff),token.puncHandler(curr));
							buff.clear();
							run = false;					
						}
						else
						{
							token.setTk(toString(buff),token.puncHandler(curr));
							buff.clear();
							run = false;
						}
					}//else
				}
				break;	
			case lit:
				if(Character.isWhitespace(curr))
					currState = State.lit;
				if(Character.isDigit(curr)) 
					buff.add(curr);
				if(Character.isLetter(curr))
					buff.add(curr);
				if(!buff.isEmpty() && buff.get(0) == '0')
				{
					run = false;
					token.setTk(toString(buff),2);  // lit token
					buff.clear();	
				}
				if(Character.isWhitespace(next) || next == '\n' || token.isPunctuation(next)||Character.isLetter(next))
				{
					if(next == '+' || next == '-' || next == '*' || next =='/')
					{
						run = false;
						if(token.isKeywords(toString(buff)))
							token.setTk(toString(buff), 1); //token
						else
							token.setTk(toString(buff),2);  //lit token
						buff.clear();
						
					}
					else if(next == ';')
					{
						run = false;
						if(Character.isLetter(toString(buff).charAt(0)))
							token.setTk(toString(buff),1); // id token
						if(Character.isDigit(toString(buff).charAt(0)))
							token.setTk(toString(buff),2);  // lit token
						currState = State.str;
						buff.clear();
					}
					else if(next == ')')
					{
						run = false;
						token.setTk(toString(buff),2);  // lit token	
						currState = State.rp;
						buff.clear();
					}
					else if(Character.isWhitespace(next) && !buff.isEmpty())
					{
						run = false;
						if(Character.isLetter(toString(buff).charAt(0)))
							token.setTk(toString(buff), 1); //id token
						else
							token.setTk(toString(buff),2);  //lit token
						buff.clear();
					}
					else if(Character.isLetter(next))
						currState = State.str;
				}
				if(token.isPunctuation(curr))
				{
					run = false;
					buff.add(curr);
					token.setTk(toString(buff),token.puncHandler(curr));
					buff.clear();
				}
				break;
			case lp:
				if(Character.isWhitespace(curr))
					currState = State.lp;
				if(Character.isLetter(curr))
					buff.add(curr);
				if(Character.isWhitespace(next))
				{
					run = false;
					token.setTk(toString(buff),1);
					buff.clear();
					currState = State.str;
				}
				if(Character.isDigit(curr))
				{
					buff.add(curr);
					currState = State.lit;
				}
				if(token.isPunctuation(curr) || token.isPunctuation(next))
				{
					if(curr ==')')
					{
						token.setTk(toString(buff),1);  // id token
						buff.clear();
						run = false;
						currState = State.rp;
					}
					if(curr == '"')
					{
						buff.clear();
						buff.add(curr);
						currState = State.inquot;
					}
					if(token.puncHandler(curr)== 4 ||token.puncHandler(curr)== 5 ||token.puncHandler(curr)== 6)
					{
						token.setTk(toString(buff),1);  // id token
						buff.clear();
						run = false;
					}
					if(next == ')' || token.puncHandler(next)== 4 ||token.puncHandler(next)== 5 ||token.puncHandler(next)== 6)
					{
						token.setTk(toString(buff),1);  // id token
						buff.clear();
						run = false;
						currState = State.rp;
					}
				}
				break;
				
			case rp:
				buff.add(curr);
				token.setTk(toString(buff),token.puncHandler(curr));
				currState = State.str;
				run = false;
				buff.clear();	
				break;
			case inquot:
				buff.add(curr);
				if(curr == '"')
				{
					token.setTk(toString(buff),2);  // lit token
					run = false;
					buff.clear();
				}
				if(next == ')')
					currState = State.rp;
				if(next == ';')
					currState = State.str;
				break;
			default:
				run = false;
				err.add(new Error(buffer.getLineNo(),"unrecognize character"));
				break;
				
			}
		}//while
		if(next == '\n')
		{
			currState = State.str;
		}
		token.enray(token.getTk());
		return token.getTk();	
	}//getToken()

	public Token getT()
	{
		return this.token;
	}
	// give a instance of token class
	
	public Buffer getB()
	{
		return this.buffer;
	}
	public void run() //tester
	{
		Token tk = getToken();
		do
		{
			System.out.println(tk.toString());
		}
		while((tk = getToken()).getTkNum()!= 18);
		System.out.println(tk.toString());
		for(Error e : err)
		{
			System.out.println(e);
		}
	}
}
