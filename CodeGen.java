package Scanner;
/*
 * @author: Huaqing Liu
 * this class is to give some helper method to generate intermeida code
 * it will generate label for assembly code
 * 
 * Further coding:
 * if new loop feather will be added new, it will need more label function
 * known bugs: none
 * 
 * unimplementation instructions: none
 */
public class CodeGen {

	private String ifLabel;
	private String whileLable;
	private String endWhile;
	private String writeLable;
	private int count;
	
	public CodeGen()
	{
		this.ifLabel = "ifLable";
		this.whileLable = "whileLable";
		this.writeLable = "writeLable";
		this.endWhile = "endWhile";
		this.count = 0;
	}
	
	public String getIf()
	{
		this.count++;
		return new String(this.ifLabel + count);
	}
	
	public String getWhile()
	{
		this.count++;
		return new String(this.whileLable+count);
	}
	
	public String getEndWhile()
	{
		this.count++;
		return new String(this.endWhile+count);
	}
	
	public String getWrite()
	{
		this.count++;
		return new String(this.writeLable+count);
	}
	
	public String comment(String str)
	{
		return new String("# " + str);
	}
}
