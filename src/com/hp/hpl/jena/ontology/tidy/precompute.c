

#include <stdio.h>
#include <stdlib.h>

#include <assert.h>
#define null 0
#define MAX_SYM 1024
#define MAX_SET 5000
#define MAX_TRIPLES 2000
#define O(x)  ((x)&1023)
#define P(x)  (((x)>>10)&1023)
#define S(x)  (((x)>>20)&1023)
int triples[MAX_TRIPLES];
int tCnt = 0;
char * symbols[MAX_SYM];
int symCnt = 0;
int * sets[MAX_SET];
int setCnt = 0;

void saveSet(const int * set,int sz) {
	assert(setCnt<MAX_SET);
	sets[setCnt] = malloc(sizeof(int)*(sz+1));
	memcpy(sets[setCnt]+1,set,sizeof(int)*sz);
	sets[setCnt][0]=sz;
	setCnt++;
}

int tcmp(const void * a, const void * b) {
	return *(int*)a - *(int*)b;
}

char * allocStr(const char *buf) {
	char * rslt = malloc(strlen(buf)+1);
	strcpy(rslt,buf);
	return rslt;
}

int symlookup(const char * str) {
	int i;
	for (i=0;i<symCnt;i++) {
		if ( !strcmp(symbols[i],str) )
			return i;
	}
	printf("Bad input '%s'\n",str);
	assert(!"Bad input");
}

void read() {
   char buf[256];
   char buf1[256];
   char buf2[256];
   char buf3[256];
   int s,p,o;
   while ( gets(buf) != null ) {
	   if ( strcmp(buf,"%%") ) {
               symbols[symCnt++] = allocStr(buf);
	   } else break;
   }
   while ( gets(buf) != null ) {
	   if ( strcmp(buf,"%%") ) {
	   sscanf(buf,"%s %s %s",buf1,buf2,buf3);
	   s = symlookup(buf1);
	   p = symlookup(buf2);
	   o = symlookup(buf3);
	   assert(tCnt < MAX_TRIPLES);
	   triples[tCnt++] = (s<<20) | (p <<10) | o;
	   } else break;
   }
   qsort(triples,tCnt,sizeof(int),tcmp);
   while ( gets(buf) != null ) {
	   char * b, * e;
	   int i;
	   int set[500];
	   char * xx = buf + strlen(buf);
	   i=0;
	   for ( b = buf; b < xx; b=e+1) {
	   for (e=b; *e!=0 && *e!=' '; e++);
	   if ( e==b) continue;
	   *e = 0;
	   set[i++] = symlookup(b);
	   }
	   saveSet(set,i);
   }
}


void dump() {
	int i,j;
	for (i=0;i<symCnt;i++) 
		printf("Symbol %d = '%s'\n",i,symbols[i]);
	for (i=0;i<tCnt;i++)
		printf("T: %s %s %s\n",
				symbols[S(triples[i])],
				symbols[P(triples[i])],
				symbols[O(triples[i])]);
	for (i=0;i<setCnt;i++) {
		printf("Set %d = {",i);
		for (j=1;j<=sets[i][0];j++)
			printf(" %s",symbols[sets[i][j]]);
		printf(" }\n");
	}
}


int main(int argc,char**argv) {
	read();
	dump();


}



