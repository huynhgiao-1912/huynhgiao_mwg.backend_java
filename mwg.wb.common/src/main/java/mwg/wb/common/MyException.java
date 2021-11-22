package mwg.wb.common;

public class MyException extends Exception{
	   String str1;
	   /* Constructor of custom exception class
	    * here I am copying the message that we are passing while
	    * throwing the exception to a string and then displaying 
	    * that string along with the message.
	    */
	   public MyException(String s) 
	    { 
	      
	        super(s);
	        str1=s;
	    } 
	   public String toString(){ 
		return ("MyException : "+str1) ;
	   }
	}