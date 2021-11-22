package mwg.wb.pkg.gameapp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.*;
import java.util.stream.Collectors;

import com.google.common.base.Strings;

import mwg.wb.model.products.ProductErpPriceBO;
public class test {
	public static void main(String[] args) {
		 List<testModel> list = new ArrayList<testModel>();
	        list.add(new testModel(2,"HẠNH"));
	        list.add(new testModel(1,"HIHI"));
	        list.add(new testModel(4,null));
	        list.add(new testModel(5,"Hưng"));
	        
	        
	         
	        
	        //list = list.stream().sorted(Comparator.comparing(x -> Strings.isNullOrEmpty(x.IMG))).collect(Collectors.toList());
	        list = list.stream().sorted(Comparator.<testModel>comparingInt(x -> Strings.isNullOrEmpty(x.IMG) ? 1 : 0 )
					.thenComparingInt(x -> x.STT)).collect(Collectors.toList()); // desending

	        
	        for(var item : list){
	            System.out.println(item.STT + " - ");
	        }
	        System.out.println("Hello World");
	}
}

class testModel{
	public testModel(int st,String im) {
		STT = st;
		IMG = im;
	}
	public int STT;
	public String IMG;
}
