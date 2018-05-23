package reqGen.ncsu.util;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

/** This a utility that will find a method object of a class
  * given its name and parameters.
  */
public class MethodLocator
{
   /** The class whose method is to be located. */
   private final Class targetClass;

   /** For each method name, it stores a vector of Method objects with 
     * the given name and uses the method name as its key.
     */
   private final Hashtable methodTable = new Hashtable();

   /** Logger */
   private static Logger logger = Logger.getLogger(MethodLocator.class);

   /** Constructs a method locator object for the given class.
     * It uses a Hashtable object to store the methods and method
     * parameters for later use. For each method name, the methodTable stores a
     * vector of Method objects with the same name and uses the method name as
     * its key.
     * @param c  the Class in which this object will locate methods
     */
   public MethodLocator(Class c)
   {
      targetClass = c;

      //get all the class's Method objects
      Method[] methods = targetClass.getMethods();

      for (int i = 0; i < methods.length; i++)
      {
         logger.debug(methods[i].getName());
         String methodName = methods[i].getName();

         //see if the Methods vector for the give method name exist.
         Vector v = (Vector) methodTable.get(methodName);
         if (v == null)
         {
            //not yet exist, so create one and add it to the table
            v = new Vector();
            methodTable.put(methodName, v);
         }

         //add the method to the vector
         v.addElement(methods[i]);
      }        
   }

   /** Find the method with the specified name and the given input paramaters. 
     * @param methodName the name of the method to be located
     * @param args  the parameters of the method
     * @return the Method object that matches the given name and the parameter
     */
   public Method findMethod(String methodName, Object[] args) throws NoSuchMethodException
   {
      logger.debug("findMethod " + methodName);

      // get all methods with the given name
      Vector methods = (Vector) methodTable.get(methodName);

      if (methods == null) //no such method
         throw new NoSuchMethodException(methodName);

      // we found a methods vector for the given method name.
      // try to find the method with the same signature as specified by args.	
      for (int i = 0; i < methods.size(); i++)
      {
         Method method = (Method)methods.elementAt(i);

         //test to see if this is the method
         if (isCompatibleMethod(method,args))
            return method;
         else
            continue; //try the next method
      }

      // we did not find the method
      throw new NoSuchMethodException(methodName);
   }

   /** Test whether the method is compatible with the given
     * input paramaters. It is compatible if the number
     * of the arguments are the same and the types in the input 
     * parameters are assignable to the method's argument type.
     * Note that a primitive Wrapper can be assigned to its primitive
     * counterpart.  
     * @param method the method to be tested
     * @param inputParam  the parameters to be tested
     * @return whether we can invoke the method with the given input params
     */
   private boolean isCompatibleMethod(Method method, Object[] inputParam) 
   {
      //get the method parameter types
      logger.debug("isCompatibleMethod 1 " + method.getName());
      Class[] paramTypes = method.getParameterTypes();

      //if the method we are looking for has no argument
      if (inputParam == null)
      {
         logger.debug("isCompatibleMethod 2 " + method.getName());

         //if the method method has no arguement then m is the method
         if (paramTypes == null || paramTypes.length ==0)
            return true;
         else // it can not the method, so return false
            return false;   
      }

      //if the number of arguments are different
      logger.debug("isCompatibleMethod 3 " + method.getName());
      if (paramTypes.length != inputParam.length)
         return false;

      //now, we have the same number of arguments, compare the argument types
      logger.debug("isCompatibleMethod 4 " + method.getName());
      for (int j=0;j<paramTypes.length;j++)
      {
         if (inputParam[j] == null)
         {
            throw new IllegalArgumentException("Illegal arguments MethodLocator.findMethod(String, Object[]), method name is "
                                               + "\"" +  method.getName()+ "\"" + ". Object[" + j + "] is null");
         }

         if (isCompatibleParamTypes( paramTypes[j], inputParam[j].getClass()))
            continue;
         else
            return false;            
      }

      //still here? It means all the parameters are "matching".
      logger.debug("isCompatibleMethod 5 " + method.getName());
      return true;
   }

   /** A private help function to test whether the two types are compatible
     * when used to invoke a method using reflection.
     * @param methodParamClass the declared param type
     * @param inputParamClass the input param type
     * @return true if they are compatible, false otherwise
     */
   private boolean isCompatibleParamTypes(Class methodParamClass, Class inputParamClass) 
   {
      logger.debug("isCompatibleParamTypes 1 " + methodParamClass + " " + inputParamClass);

      //if the method's parameter is a primitive
      if (methodParamClass.isPrimitive())
      {
         logger.debug("isCompatibleParamTypes 2 " + methodParamClass + " " + inputParamClass);

         if (methodParamClass.equals(Integer.TYPE))
            return(inputParamClass.equals(Integer.class));

         else if (methodParamClass.equals(Double.TYPE))
            return(inputParamClass.equals(Double.class));

         else if (methodParamClass.equals(Long.TYPE))
            return(inputParamClass.equals(Long.class));

         else if (methodParamClass.equals(Boolean.TYPE))
            return(inputParamClass.equals(Boolean.class));

         else if (methodParamClass.equals(Character.TYPE))
            return(inputParamClass.equals(Character.class));

         else if (methodParamClass.equals(Byte.TYPE))
            return(inputParamClass.equals(Byte.class));

         else if (methodParamClass.equals(Float.TYPE))
            return(inputParamClass.equals(Float.class));

         else if (methodParamClass.equals(Short.TYPE))
            return(inputParamClass.equals(Short.class));
      }
      else // not a primitive
      {
         logger.debug("isCompatibleParamTypes 3 " + methodParamClass + " " + inputParamClass);
         return(methodParamClass.isAssignableFrom(inputParamClass));
      }

      logger.debug("isCompatibleParamTypes 4 " + methodParamClass + " " + inputParamClass);
      return false;
   }

   /* commented out to save space
   public static void main(String[] args1) 
   {
      String testString = "This Is My Test String";

      MethodLocator locator = new MethodLocator(testString.getClass());

      Object[] args = new Object[] {new Integer(5), new Integer(15)};
      try
      {
         //test for String.substring(int, int)
         Method m = locator.findMethod("substring",args );
         System.out.println( "The substring = " +(String)m.invoke(testString, args) );

         //test String.toUpperCase()
         m = locator.findMethod("toUpperCase",null );
         System.out.println( "The UpperCase = " +(String)m.invoke(testString, null) );

         //test String.startsWith(String, int)
         args = new Object[] {"My", new Integer(8)};
         m = locator.findMethod("startsWith", args );
         System.out.println( "The test String starts with \"My\" at index 8 is  = " 
                             +(Boolean)m.invoke(testString, args) );

      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   */
}

