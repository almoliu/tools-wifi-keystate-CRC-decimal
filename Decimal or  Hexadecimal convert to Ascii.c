/*
 *  *Function:		itoa
 *  *Description:	Decimal to Ascii
 *  *Parameter:     num:the number ready  to convert to ascii
 *  *Return:		the ascii or string
 *  *Author:		almo 
 *  *Date:	       20150804
 *  *modify:       
 *  */

char *itoa(int  num, char *string)
{ 
    char *ptr = string;
    
    if(num<0)
    {
    	 num *=-1;
    	*ptr++ = '-';
    }
    
    if(num<10)
    {
       *ptr++ = num + '0';/*'0' is the corresponding zero and its hexadecimal is 0x30 , Decimal is 48 */
       *ptr  = '\0';/* the end of a string*/
    }
    else {
             itoa(num/10,ptr);
             
             while(*ptr!='\0')
              	ptr++;
            
             *ptr++ = num%10 + '0';
             *ptr='\0';
    }

    return string;
}


/*
 *  *Function:		xtoa
 *  *Description:	Hexadecimal to Ascii
 *  *Parameter:     num:the number ready  to convert to ascii
 *  *Return:		the ascii or string
 *  *Author:		almo 
 *  *Date:	       20150804
 *  *modify:       
 *  */
 

 char *xtoa(int num,char *string)
 {
 	 char *ptr = string;

 	if(num<10)
 	{
 		*ptr++= num + '0';
 		*ptr = '\0';	
 	}
 	else if((num>=10)&&(num<16))
 	{
 		 *ptr++=num - 10 + 'A';
 		 *ptr = '\0';
 	}
 	else{

 		xtoa(num/16,ptr);
 		
 		while(*ptr!='\0')
 			ptr++;
 			
 		*ptr++ = num%16-10+'A';
 		*ptr = '\0';
 	}
 	return string;
 }