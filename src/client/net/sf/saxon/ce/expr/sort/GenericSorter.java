package client.net.sf.saxon.ce.expr.sort;

/**
Generically sorts arbitrary shaped data (for example multiple arrays, 1,2 or 3-d matrices, and so on) using a
quicksort or mergesort. This class addresses two problems, namely
<ul>
  <li><i>Sorting multiple arrays in sync</i>
  <li><i>Sorting by multiple sorting criteria</i> (primary, secondary, tertiary,
	...)
</ul>
<h4>Sorting multiple arrays in sync</h4>
<p>
Assume we have three arrays X, Y and Z. We want to sort all three arrays by
  X (or some arbitrary comparison function). For example, we have<br>
  <tt>X=[3, 2, 1], Y=[3.0, 2.0, 1.0], Z=[6.0, 7.0, 8.0]</tt>. The output should
  be <tt><br>
  X=[1, 2, 3], Y=[1.0, 2.0, 3.0], Z=[8.0, 7.0, 6.0]</tt>. </p>
<p>How can we achive this? Here are several alternatives. We could ... </p>
<ol>
  <li> make a list of Point3D objects, sort the list as desired using a comparison
	function, then copy the results back into X, Y and Z. The classic object-oriented
	way. </li>
  <li>make an index list [0,1,2,...,N-1], sort the index list using a comparison function,
	then reorder the elements of X,Y,Z as defined by the index list. Reordering
	cannot be done in-place, so we need to copy X to some temporary array, then
	copy in the right order back from the temporary into X. Same for Y and Z.
  </li>
  <li> use a generic quicksort or mergesort which, whenever two elements in X are swapped,
	also swaps the corresponding elements in Y and Z. </li>
</ol>
Alternatives 1 and 2 involve quite a lot of copying and allocate significant amounts
of temporary memory. Alternative 3 involves more swapping, more polymorphic message dispatches, no copying and does not need any temporary memory.
<p> This class implements alternative 3. It operates on arbitrary shaped data.
  In fact, it has no idea what kind of data it is sorting. Comparisons and swapping
  are delegated to user provided objects which know their data and can do the
  job.
<p> Lets call the generic data <tt>g</tt> (it may be one array, three linked lists
  or whatever). This class takes a user comparison function operating on two indexes
  <tt>(a,b)</tt>, namely an {@link Sortable}. The comparison function determines
  whether <tt>g[a]</tt> is equal, less or greater than <tt>g[b]</tt>. The sort,
  depending on its implementation, can decide to swap the data at index <tt>a</tt>
  with the data at index <tt>b</tt>. It calls a user provided {@link Sortable}
  object that knows how to swap the data of these indexes.
<p>The following snippet shows how to solve the problem.
<table>
<td class="PRE">
<pre>
final int[] x;
final double[] y;
final double[] z;

x = new int[]    {3,   2,   1  };
y = new double[] {3.0, 2.0, 1.0};
z = new double[] {6.0, 7.0, 8.0};


// this one knows how to swap two indexes (a,b)
Swapper swapper = new Swapper() {
&nbsp;&nbsp;&nbsp;public void swap(int a, int b) {
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;int t1;	double t2, t3;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;t1 = x[a]; x[a] = x[b];	x[b] = t1;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;t2 = y[a]; y[a] = y[b]; y[b] = t2;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;t3 = z[a]; z[a] = z[b];	z[b] = t3;
&nbsp;&nbsp;&nbsp;}
};
// simple comparison: compare by X and ignore Y,Z<br>
IntComparator comp = new IntComparator() {
&nbsp;&nbsp;&nbsp;public int compare(int a, int b) {
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return x[a]==x[b] ? 0 : (x[a]&lt;x[b] ? -1 : 1);
&nbsp;&nbsp;&nbsp;}
};

System.out.println("before:");
System.out.println("X="+Arrays.toString(x));
System.out.println("Y="+Arrays.toString(y));
System.out.println("Z="+Arrays.toString(z));

GenericSorting.quickSort(0, X.length, comp, swapper);
// GenericSorting.mergeSort(0, X.length, comp, swapper);

System.out.println("after:");
System.out.println("X="+Arrays.toString(x));
System.out.println("Y="+Arrays.toString(y));
System.out.println("Z="+Arrays.toString(z));
</pre>
</td>
</table>
<h4>Sorting by multiple sorting criterias (primary, secondary, tertiary, ...)</h4>
<p>Assume again we have three arrays X, Y and Z. Now we want to sort all three
  arrays, primarily by Y, secondarily by Z (if Y elements are equal). For example,
  we have<br>
  <tt>X=[6, 7, 8, 9], Y=[3.0, 2.0, 1.0, 3.0], Z=[5.0, 4.0, 4.0, 1.0]</tt>. The
  output should be <tt><br>
  X=[8, 7, 9, 6], Y=[1.0, 2.0, 3.0, 3.0], Z=[4.0, 4.0, 1.0, 5.0]</tt>. </p>
<p>Here is how to solve the problem. All code in the above example stays the same,
  except that we modify the comparison function as follows</p>
<table>
<td class="PRE">
<pre>
//compare by Y, if that doesn't help, reside to Z
IntComparator comp = new IntComparator() {
&nbsp;&nbsp;&nbsp;public int compare(int a, int b) {
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (y[a]==y[b]) return z[a]==z[b] ? 0 : (z[a]&lt;z[b] ? -1 : 1);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return y[a]&lt;y[b] ? -1 : 1;
&nbsp;&nbsp;&nbsp;}
};
</pre>
</td>
</table>

<h4>Notes</h4>
<p></p>
<p> Sorts involving floating point data and not involving comparators, like, for
  example provided in the JDK {@link java.util.Arrays} and in the Colt
  (cern.colt.Sorting) handle floating point numbers in special ways to guarantee
  that NaN's are swapped to the end and -0.0 comes before 0.0. Methods delegating
  to comparators cannot do this. They rely on the comparator. Thus, if such boundary
  cases are an issue for the application at hand, comparators explicitly need
  to implement -0.0 and NaN aware comparisons. Remember: <tt>-0.0 < 0.0 == false</tt>,
  <tt>(-0.0 == 0.0) == true</tt>, as well as <tt>5.0 &lt; Double.NaN == false</tt>,
  <tt>5.0 &gt; Double.NaN == false</tt>. Same for <tt>float</tt>.
<h4>Implementation </h4>
<p>The quicksort is a derivative of the JDK 1.2 V1.26 algorithms (which are, in
  turn, based on Bentley's and McIlroy's fine work).
  The mergesort is a derivative of the JAL algorithms, with optimisations taken from the JDK algorithms.
Both quick and merge sort are "in-place", i.e. do not allocate temporary memory (helper arrays).
Mergesort is <i>stable</i> (by definition), while quicksort is not.
A stable sort is, for example, helpful, if matrices are sorted successively
by multiple columns. It preserves the relative position of equal elements.

@author wolfgang.hoschek@cern.ch
@version 1.0, 03-Jul-99
*/
public class GenericSorter extends Object {

	private static final int SMALL = 7;
	private static final int MEDIUM = 7;
    private static final int LARGE = 40;


	/**
	 * Makes this class non instantiable, but still let's others inherit from it.
	 */
	protected GenericSorter() {}

	/**
	 * Sorts the specified range of elements according
	 * to the order induced by the specified comparator.  All elements in the
	 * range must be <i>mutually comparable</i> by the specified comparator
	 * (that is, <tt>c.compare(a, b)</tt> must not throw an
	 * exception for any indexes <tt>a</tt> and
	 * <tt>b</tt> in the range).<p>
	 *
	 * The sorting algorithm is a tuned quicksort,
	 * adapted from Jon L. Bentley and M. Douglas McIlroy's "Engineering a
	 * Sort Function", Software-Practice and Experience, Vol. 23(11)
	 * P. 1249-1265 (November 1993).  For details, see
	 * http://citeseer.ist.psu.edu/bentley93engineering.html.
     * This algorithm offers n*log(n) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance.
	 *
	 * @param fromIndex the index of the first element (inclusive) to be sorted.
	 * @param toIndex the index of the last element (exclusive) to be sorted.
	 * @param c the comparator to determine the order of the generic data;
	 *   an object that knows how to swap the elements at any two indexes (a,b).
	 *
	 */
	public static void quickSort(int fromIndex, int toIndex, Sortable c) {
		quickSort1(fromIndex, toIndex-fromIndex, c);
	}

	/**
	 * Sorts the specified sub-array into ascending order.
	 */
	private static void quickSort1(int off, int len, Sortable comp) {
		// Insertion sort on smallest arrays
		if (len < SMALL) {
			for (int i=off; i<len+off; i++)
				for (int j=i; j>off && (comp.compare(j-1,j)>0); j--) {
				    comp.swap(j, j-1);
				}
			return;
		}

		// Choose a partition element, v
		int m = off + (len >>> 1); // len/2;       // Small arrays, middle element

		if (len > MEDIUM) {
			int l = off;
			int n = off + len - 1;
			if (len > LARGE) {        // Big arrays, pseudomedian of 9
				int s = len >>> 3; // len/8;
				l = med3(l,     l+s, l+2*s, comp);
				m = med3(m-s,   m,   m+s, comp);
				n = med3(n-2*s, n-s, n, comp);
			}
//			m = med3(l, m, n, comp); // Mid-size, med of 3
			// manually inlined (most time is spent near the leafs of the recursion tree)
			//a = comp.compare(l,m);
			//b = comp.compare(l,n);
			int c = comp.compare(m,n);
			m = (comp.compare(l,m)<0 ?
				(c<0 ? m : comp.compare(l,n)<0 ? n : l) :
				(c>0 ? m : comp.compare(l,n)>0 ? n : l));
		}
		//long v = x[m];

		// Establish Invariant: v* (<v)* (>v)* v*
		int a = off, b = a, c = off + len - 1, d = c;
		while (true) {
			int comparison;
			while (b <= c && ((comparison=comp.compare(b,m))<=0)) {
				if (comparison == 0) {
					if (a==m) m = b; // pivot is moving target; DELTA to JDK !!!
					else if (b==m) m = a; // pivot is moving target; DELTA to JDK !!!
				    comp.swap(a++, b);
				}
				b++;
			}
			while (c >= b && ((comparison=comp.compare(c,m))>=0)) {
				if (comparison == 0) {
					if (c==m) m = d; // pivot is moving target; DELTA to JDK !!!
					else if (d==m) m = c; // pivot is moving target; DELTA to JDK !!!
				    comp.swap(c, d--);
				}
				c--;
			}
			if (b > c) break;
			if (b==m) m = d; // pivot is moving target; DELTA to JDK !!!
			else if (c==m) m = c; // pivot is moving target; DELTA to JDK !!!
			comp.swap(b++, c--);
		}

		// Swap partition elements back to middle

		int s = Math.min(a-off, b-a  );
		// vecswap(swapper, off, b-s, s);
		// manually inlined
		int aa = off; int bb = b-s;
		while (--s >= 0) comp.swap(aa++, bb++);
        int n = off + len;
		s = Math.min(d-c,   n-d-1);
		// vecswap(swapper, b,   n-s, s); // manually inlined
		aa = b; bb = n-s;
		while (--s >= 0) comp.swap(aa++, bb++);

		// Recursively sort non-partition-elements
		if ((s = b-a) > 1)
			quickSort1(off, s, comp);
		if ((s = d-c) > 1)
			quickSort1(n-s, s, comp);
	}

	/**
	 * Returns the index of the median of the three indexed elements.
	 */
    private static int med3(int a, int b, int c, Sortable comp) {
            int bc = comp.compare(b,c);
            return (comp.compare(a,b)<0 ?
                (bc<0 ? b : comp.compare(a,c)<0 ? c : a) :
                (bc>0 ? b : comp.compare(a,c)>0 ? c : a));
        }



}

// This class derives from work by Wolfgang Hoschek at CERN, and is available under a BSD-style
// license from CERN.

/*
Copyright ? 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, moNumbererdify, distribute and sell this software and its documentation for any purpose
is hereby granted without fee, provided that the above copyright notice appear in all copies and
that both that copyright notice and this permission notice appear in supporting documentation.
CERN makes no representations about the suitability of this software for any purpose.
It is provided "as is" without expressed or implied warranty.
*/

// Modified by Michael Kay to use the Saxon Sortable interface and to remove unused code


