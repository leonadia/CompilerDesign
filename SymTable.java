package Scanner;

import java.util.ArrayList;
/*
*Huaqing Liu
*I will struct SymTable as a Tree
*every block is a node of tree
*inner-block will be the child node of outer-block 
*I insist to make tree for reason
*first, easy to get the variable environment
*2nd, it can check duplicate of variable slightly faster, because it does not need to check variable which is not in environment and get environment faster
*3rd, it also can check unmatched begin-ends very quickly
*only problem is to print the tree
*to make printing tree eazier, I also make a arrayList to store every table
*As discussing in class, variable will be added at declaration, and new node will be created when blcokst
 * 
 * known bugs: none
 * 
 * unimplementat instruction: none
 */
public class SymTable {

	private Node root;
	private Node curr;
	public int offset;
	private ArrayList<Node> tableList = new ArrayList<Node>();
	public SymTable()
	{
		this.root = null;
		this.curr = null;
		this.offset = 0;
	}
	//this function will just create new Node and point curr and last to the correct position
	//this function will be called every time a new block being reached
	public Node createNode()
	{
		Node n = new Node();
		if(root == null)
		{
			n.pn = null;
			root = n;
			curr = n;
		}
		else
		{
			curr.cn.add(n);
			n.pn = curr;
			curr = n;
		}
		return n;
	}
	//this function will point curr to the parent node, and last point to the parent of curr node
	//this function will be called when End being reached
	public void backNode()
	{
		if(curr != root)
			curr = curr.pn;
	}

	//this function will be called in declaration
	//this function will add value into Hash table
	public void insert(String key, String type, int offset)
	{
		curr.symTable.put(key,new Atrribute(type,offset));
	}
	//find token from all scope, it will return attribute of identifier, type and offset
	private Atrribute findAllScope(String key)
	{
		Node tmp = curr;
		Atrribute a = new Atrribute(" ",-1);
			while(tmp != null)
			{
				if(tmp.symTable.containsKey(key))
				{
					a = tmp.symTable.get(key);
					break;
				}
				tmp = tmp.pn;
			}
		return a;	
	}
	//find token in current scope, it will return the attribute of identifier, type and offset
	public Atrribute findCurrScope(String key)
	{
		if(curr.symTable.contains(key))
			return curr.symTable.get(key);
		else
			return new Atrribute(" ", -1);
	}
	//I combine findALL and findCurr into one function
	//this function will check current block first, if there is not matched identifier
	//it will go to the outer block until there is not outer block
	//also, this function will guarantee the local variable will be used first
	//this function will not use at declaration
	public Atrribute find(String key)  
	{
		Atrribute a = new Atrribute(" ",-1);
		if(this.findCurrScope(key).offset != -1)
		{
			a = this.findCurrScope(key);
			return a;
		}
		else if(this.findAllScope(key).offset != -1)
		{
			a = this.findAllScope(key);
			return a;
		}
		else
			return a;
	}
	//this function will check type of both side of assignment token
	//if same, it will return true
	//if different, it will return false
	public boolean typeComparasion(String LHS, String RHS)
	{
		if(find(RHS).offset != -1)
			return find(LHS).type.equals(find(RHS).type);
		else
		{
			return find(LHS).type.equals(typeCheck(RHS));
		}
	}
	//this function will determine literal value type
	private String typeCheck(String str)
	{
		String type = " ";
		if(str.equals("TRUE") || str.equals("FALSE"))
			type = "LOGICAL";
		else if(str.charAt(0) == '"')
			type = "STRING";
		else if(Character.isDigit(str.charAt(0)))
		{
			type = "INTEGER";
			for(char c : str.toCharArray())
			{
				if(!Character.isDigit(c))
				{
					type = "E";
				}
			}
		}
		else
			type = "E";
		
		return type;
	}
	
	//Maintain a list of all scope, this is esay to print out
	public void setTL(Node n)
	{
		this.tableList.add(n);
	}
	
	public ArrayList<Node> getTL()
	{
		return this.tableList;
	}
	// to print out the symbol table
	public void print()
	{
		for(Node n : this.tableList)
		{
			for(String key : n.symTable.keySet())
			{
				System.out.println(key+" "+n.symTable.get(key).type+" " +n.symTable.get(key).offset);
			}
		}
	}
	
}
