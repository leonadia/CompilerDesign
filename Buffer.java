package Scanner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
/*
 * @author: Huaqing Liu
 * this class is to read and store character into the buffer
 * I am using double pair buff
 * it works like queue, first in and first out, it will store utmost 3 tokens
 * 
 * for further coding: none
 * 
 * known bugs: none
 * 
 * unimplement insturction: none
 */
public class Buffer {
	private String buffer;  //this is buffer, it will be turn in to char array late
	private char backUp;
	private FileReader fr;
	private BufferedReader br;
	private int pos;
	private int lineNo;
	
	public Buffer(FileReader fr)
	{
		this.buffer = " ";
		this.backUp = ' ';
		pos = 0;
		lineNo = 0;
		this.fr = fr;
		this.br = new BufferedReader(this.fr);
	}
	//this function will return 1 char at time
	public char getChar()
	{
		this.setBackUp(buffer.charAt(pos));  // set up backup
		pos++;
		if(pos >= buffer.length())
		{
			try 
			{
				buffer = br.readLine();
			} catch (IOException e) 
			{
				e.printStackTrace();
			}//try
			if(buffer == null)
				System.err.println("Empty line");
		    setPos(0);
		    setLineNo(getLineNo() + 1);   
		    buffer = buffer + "\n";  
		}//if
    	return buffer.charAt(getPos()); 
	}
	
	//helper functions
	public String getBuffer() {
		return buffer;
	}
	public void setBuffer(String buffer) {
		this.buffer = buffer;
	}
	public char getBackUp() {
		return backUp;
	}

	public void setBackUp(char backUp) {
		this.backUp = backUp;
	}

	public int getLineNo() {
		return lineNo;
	}

	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}
	public void setPos(int i)
	{
		this.pos = i;
	}
	public int getPos()
	{
		return this.pos;
	}
	
	public boolean EOF() throws IOException
	{
		return br.readLine() == null;
	}
}
