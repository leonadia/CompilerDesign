package Scanner;
/*
 * @Author: Huaqing Liu
 * this class is to build attribute of an identifier
 * it store type and location(offset in stack) of an identifer
 * 
 * further coding: it may be added some new operation
 * 
 * known bugs: none
 * 
 * unimplement instruction: none
 */
public class Atrribute {

	public String type;
	public int offset;
	
	public Atrribute(String type, int offset)
	{
		this.type = type;
		this.offset = offset;
	}	
}
