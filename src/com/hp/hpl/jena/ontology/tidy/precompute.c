

#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include <string.h>

#include <assert.h>
#define null 0
#define MAX_SYM 1024
#define MAX_SET 5000
#define MAX_TRIPLES 2000
#define O(x)  ((x)&1023)
#define P(x)  (((x)>>10)&1023)
#define S(x)  (((x)>>20)&1023)
#define SPO(s,p,o) (S(s)|P(p)|O(o))
#define MAX_RESULTS 500000
int results[MAX_RESULTS][6];
int rCnt = 0;
int triples[MAX_TRIPLES];
int tCnt = 0;
char * symbols[MAX_SYM];
int symCnt = 0;
int setCnt = 0;
int badCnt = 0;
int ist = 0;
int isntt = 0;
typedef struct _Set {
  int id;
  int sz;
  int syms[1];
  } * Set;
Set sets[MAX_SET];
Set sortedSets[MAX_SET];


int tcmp(const void * a, const void * b) {
	return *(int*)a - *(int*)b;
}

int istriple(int i) {
  int *j = bsearch(&i, triples, 
                    tCnt, sizeof(int), tcmp );
  if ( j==null || *j != i ) {
    isntt ++;
    return 0;
    }
    ist++;
  return 1;
}

int setCmp(const void * a, const void * b) {
    Set aa = *(Set*)a;
    Set bb = *(Set*)b;
    int diff = aa->sz - bb->sz;
    int i;
    for( i=0; diff == 0 && i<aa->sz; i++ )
      diff = aa->syms[i] - bb->syms[i];
	return diff;
}


int saveSet(const int * set,int sz) {
	assert(setCnt<MAX_SET);
	sets[setCnt] = malloc(sizeof(int)*(sz+2));
	sets[setCnt]->id = setCnt;
	sets[setCnt]->sz = sz;
	memcpy(sets[setCnt]->syms,set,sizeof(int)*sz);
	
    qsort(sets[setCnt]->syms,sz,sizeof(int),tcmp);
    sortedSets[setCnt] = sets[setCnt];
	return setCnt++;
}
/*
ok is an array of boolean the same size as in s.
return a new set consisting of the ok members of s.
return -1 if empty
*/
int saveSubSet(Set s, int * ok) {
   int _sub[MAX_SYM+2];
   Set sub = (Set)_sub;
   Set rslt;
   int j = 0;
   int i;
   for (i=0;i<s->sz;i++)
     if (ok[i]) 
       sub->syms[j++] = s->syms[i];
    sub->sz = j;
    if (j==0) {
      badCnt++;
      return -1;
    }
    rslt =  bsearch(&sub, sortedSets, 
                    setCnt, sizeof(Set), setCmp );   
    if ( rslt )
       return rslt->id;
    rslt = malloc(sizeof(int)*(sub->sz+2));
    memcpy(rslt,sub,sizeof(int)*(sub->sz+2));
    rslt->id = setCnt;
    sets[setCnt] = rslt;
    sortedSets[setCnt] = rslt;
    qsort( sortedSets, setCnt+1, sizeof(Set), setCmp );
    return setCnt++;
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
	return -1;
}

void read(void) {
   char buf[2560];
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
   if ( strcmp(buf,"%%") ) {
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
	   } else 
	     return;
	   
   }
}


void dump(void) {
	int i,j;
	for (i=0;i<symCnt;i++) 
		fprintf(stderr,"Symbol %d = '%s'\n",i,symbols[i]);
	for (i=0;i<tCnt;i++)
		fprintf(stderr,"T: %s %s %s\n",
				symbols[S(triples[i])],
				symbols[P(triples[i])],
				symbols[O(triples[i])]);
	for (i=0;i<setCnt;i++) {
		fprintf(stderr,"Set %d = {",i);
		for (j=0;j<sets[i]->sz;j++)
			fprintf(stderr," %s",symbols[sets[i]->syms[j]]);
		fprintf(stderr," }\n");
	}
}

void xform(int s, int p, int o) {
   Set subj = sets[s];
   Set prop = sets[p];
   Set obj = sets[o];
   int oks[MAX_SYM];
   int okp[MAX_SYM];
   int oko[MAX_SYM];
   int i,j,k, ss, pp, oo;
   memset(oks,0,sizeof(oks));
   memset(okp,0,sizeof(okp));
   memset(oko,0,sizeof(oko));
   for (i=0;i<subj->sz;i++)
    for (j=0; j<prop->sz;j++)
     for (k=0; k<obj->sz; k++) 
      if ( !(oks[i]&&okp[j]&&oko[k]) ) {
         if ( istriple(SPO(subj->syms[i],
                           prop->syms[j],
                           obj->syms[k])) ) {
             oks[i] = okp[j] = oko[k] = 1;
         }
      }
   ss = saveSubSet(subj,oks);
   if ( ss == -1 )
      return;
   pp = saveSubSet(prop,okp);
   assert( pp != -1 );
   oo = saveSubSet(obj,oko);
   assert( oo != -1 );
   assert(rCnt<MAX_RESULTS);
   results[rCnt][0] = s;
   results[rCnt][1] = p;
   results[rCnt][2] = o;
   results[rCnt][3] = ss;
   results[rCnt][4] = pp;
   results[rCnt][5] = oo;
   rCnt++;
}
void partial(void) {
   int upto;
   int i, j, k;
   for (upto=0;upto<setCnt;upto++) {
      fprintf(stderr,"%5d %5d %6d %7d %7d %7d\n",upto,setCnt,rCnt, badCnt,ist,isntt);
      for (i=0;i<=upto;i++)
        for (j=0;j<=upto;j++)
          for (k=0;k<=upto;k++)
            if ( i==upto || j==upto || k==upto ) {
               xform(i,j,k);
            }
   }
}

void addSingletons(void) {
  int i;
  for (i=0; i<symCnt;i++)
    saveSet(&i,1);
}
int main(int argc,char**argv) {
	read();
	
	fprintf(stderr,"X\n");
    addSingletons();
	fprintf(stderr,"Y\n");
    qsort( sortedSets, setCnt, sizeof(Set), setCmp );
	fprintf(stderr,"Z\n");
	/* dump(); */
    
    partial();

   return 0;
}



