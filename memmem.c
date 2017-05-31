#include <string.h>

/**/
void * memmem(const void *mem1,const void *mem2,unsigned char size,unsigned char len)
{	
	int i = 0;
	if(len < size)
		return NULL;
	for(i = 0;i < len - size;i++){
		if(!memcmp((void*)(mem1 + i),mem2,size))
			return (void*)(mem1 + i);
	}
	return NULL;
}








