package Scanner;
/*
 * this class will handle every token, weather it is legal or not
 * it give some helper function to help Lex class
 * legal token list:
 *letter: "a" | "A" .... "z" | "Z"
 *digit::="0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9",
 *punctuation: := "(" | ")" | ";"  |  “:=” | "."
 *infix::=  "*" | "+" | “-“ | “/”  |  "<" | ">" | "=" | "!=" | AND | OR  | DIV  | REM
 *unary operators ∷=  !
 *non_zero_digit::="1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
 *Boolean values ::= TRUE  |  FALSE
 *
 *if there is a illegal character being read, it will give a error message
 *
 *for further coding:
 *if new legal character being added, it have to be added into list
 *if new keywords being added, it have to be added in to list
 *
 *List known bug: none
 *
 *List of unimplement instruction: none
 */
import java.util.Arrays;
//to Create a Token type
public class Token {
	String name = " ";
	int tokenNum = 0;
	private String keyWordList[]; // the list of keyWords
	private char punctuationList[]; // the list for punctuation]
	private Token[] queue; //this is a double buff, it is an array with 2 element, position 0 contain current token, position 1 contain last one
						   //this works like queue, so I name it queue
	public Token()
	{
		this.name = " ";
		this.tokenNum = 0;
		this.queue = new Token[3];
		this.keyWordList = new String[] {"BEGIN","END","COMMENT","IF","THEN","WHILE","DO"
				,"INTEGER","STRING","LOGICAL","REAL","TRUE","FALSE"
				,"DIV","REM","OR","AND","READ","WRITE","WRITELN"}; 
		this.punctuationList = new char[] {'(',')',';',':','.','*','+','-','/','<','>','=','!','"'};	
	}

	public Token(String s, int i)
	{
		this.name = s;
		this.tokenNum = i;
	}
	// check String weather it is keywords or not
	public boolean isKeywords(String str)
	{
		return  Arrays.asList(keyWordList).contains(str.toUpperCase());
	}
	//this function will handle keywords and return the proper number
	public int kwHandler(String s)
	{
		int n = 0;
		if(s.equals("BEGIN"))
			n=7;
		if(s.equals("END"))
			n=8;
		if(s.equals("IF"))
			n=9;
		if(s.equals("THEN"))
			n=10;
		if(s.equals("WHILE"))
			n=11;
		if(s.equals("DO"))
			n=12;
		if(s.equals("INTEGER"))
			n=3;
		if(s.equals("STRING"))
			n=3;
		if(s.equals("LOGICAL"))
			n=3;
		if(s.equals("TRUE"))
			n=2;
		if(s.equals("FALSE"))
			n=2;
		if(s.equals("DIV"))
			n=5;
		if(s.equals("REM"))
			n=5;
		if(s.equals("READ"))
			n=13;
		if(s.equals("WRITE"))
			n=13;
		if(s.equals("WRITELN"))
			n=13;
		if(s.equals("AND"))
			n=5;
		if(s.equals("OR"))
			n=4;
		
		return n;
	}
	//this function will check token is a punctuation
	public boolean isPunctuation(char ch)
	{
		boolean b = false;
		for(char p : punctuationList)
		{
			if(ch == p)
				b = true;
		}
		return b;
	}
	//this function will handle every legal punctuation, return the proper number
	public int puncHandler(char ch)
	{
		int state = 0;
		if(ch=='+' || ch=='-')
			state = 4;
		if(ch=='*' || ch=='/')
			state = 5;
		if(ch=='!')
			state = 17;
		if(ch=='(')
			state = 14;
		if(ch ==')')
			state = 15;
		if(ch == ';')
			state = 16;
		if(ch =='.')
			state = 18;
		if(ch == '=' || ch == '<' || ch == '>')
			state = 6;
		
		return state;
	}
	
	public void setTk(String s, int i)
	{
		this.name = s;
		this.tokenNum = i;
	}
	
	public Token getTk()
	{
		return new Token(this.name,this.tokenNum) ;
	}

	public String toString()
	{
		return new String("("+this.name + "," + this.tokenNum + ")" );
	}
	  // this is enqueue function, to put the newest token into the last place of the queue
		// this will automatically dequeue when queue token come into queue
	public void enray(Token t)
	{
		if(queue[1] != null)
			queue[2] = queue[1];
		if(queue[0] != null)
			queue[1] = queue[0];
		queue[0] = t;
	}
	
	public Token getray(int i)  //to get element from queue
	{
		return queue[i];
	}
	//helper functions
	public int getTkNum()
	{
		return this.tokenNum;
	}
	public String getName()
	{
		return this.name;
	}
}
