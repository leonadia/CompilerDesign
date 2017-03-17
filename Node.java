package Scanner;

import java.util.ArrayList;
import java.util.Hashtable;
/*
*@Author: Huaqing Liu
*this class will build a Node of SymTable
*each node of tree will has a Hashtalbe to hold every Identifier as a key, and its attribute(type and loation) as value 
*each node has a Node connect to the parent Node
*each node has a ArrayList<Node> contain its child Node
*
*known buys: none
*
*unimplement instruction: none
*/
public class Node {
	public Hashtable<String,Atrribute> symTable = new Hashtable<String,Atrribute>(); 
	//Identifier will be key, and type will be value. because Identifier is unique
	public Node pn; //parent Node
	public ArrayList<Node> cn = new ArrayList<Node>(); // ArrayList of child Node
	//this class work like struct in C++, so I make everything public
	public Node()
	{
		pn = null;
	}
}
