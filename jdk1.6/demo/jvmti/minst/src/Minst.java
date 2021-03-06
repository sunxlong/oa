/*
 * %W% %E%
 * 
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright notice, 
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * 
 * Neither the name of Oracle or the names of contributors may 
 * be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL 
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST 
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY 
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, 
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */


/* Java class to hold static methods which will be called in byte code
 *    injections of all class files.
 */

public class Minst {
 
    /* Master switch that activates methods. */
    
    private static int engaged = 0; 
  
    /* At the very beginning of every method, a call to method_entry() 
     *     is injected.
     */
    
    public static void method_entry(int cnum, int mnum) {
	Class x = Minst.class;
	synchronized ( x ) {
	    if ( engaged > 0 ) {
		engaged = 0;
		String className = "Unknown";
		String methodName = "Unknown";
		Exception exp = new Exception();
		StackTraceElement[] stack = exp.getStackTrace();
                if (stack.length > 1) {
                    StackTraceElement location = stack[1];
                    className = location.getClassName();
                    methodName = location.getMethodName();
                }
		System.out.println("Reached method entry: " + 
		     className + "." + methodName + "()");
		engaged++;
	    }
	}
    }
    
}

