package org.egov.migrationkit.service;

public class Util {

	
	
	public static void main(String args[])
	
	{
		String locCode="LC-137";
		String cityCode="1025";
		String loc=  locCode.replaceAll("\\D+","");
		String mobileNumber=String.format("4%4s%5s",cityCode,"1" );
		mobileNumber=mobileNumber.replaceAll(" ", "0");
		System.out.print(mobileNumber);
	}
}
