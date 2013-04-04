/**
* Project: Project3
* Account: CS301002_12
* Author: HyryJ (Jonathan Hyry)
* Creation date: 10/09/2010
* Completion time: 3 hours
*
* Honor Code: I pledge that this program represents my
*   own program code. I received help from nobody
*   in designing and debugging my program.
*/

package com.ampsoft.chrobars.opengl;

//Import ArrayList and DecimalFormat from the JCF
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * Vec3D stores on ordered triple in an ArrayList object,
* as you would store such a group of values in a Vector
* or Array object, and provides a set of access methods
* that gets the current values for the x, y, and z
* components of the vector. It also provides methods
* that perform mathematical operations between 2 vectors,
* or on only this vector, that accept an ArrayList<float>
* object as an argument to complete the calculations that
* require another vector. The Object class' equals method
* is overridden, as the toString method has been as well.
*
* Originally created in project1; added random value
* constructor in project3.
*/
public class Vec3D
{
	/**
	 * For generating random vector objects
	 */
    private Random r = new Random();

	//Create variables for assigning values
	private float x;
	private float y;
	private float z;
	
	/**
	 * Default constructor. Initialises component 
	 * variables to zero.
	 */
	public Vec3D()
	{
		x = 0;
		y = 0;
		z = 0;
		
//		DEBUG
//		System.out.println("Default vector values of 0 assigned");
	}

	/**
	 * 
	 * @param s
	 */
    public Vec3D(String s)
    {
        if(s.equals("random"))
        {
            x = r.nextInt()*r.nextFloat();
            y = r.nextInt()*r.nextFloat();
            z = r.nextInt()*r.nextFloat();

//          DEBUG
//    		System.out.println("Random vector values assigned: x=" + x + " y=" + y + " z=" + z);
        }
        else
        {
            x = 0;
            y = 0;
            z = 0;
            
//          DEBUG
//    		System.out.println("Default vector values of 0 assigned");
        }
    }
	
	/**
	 * Overloaded constructor.
	 * 
	 * Accepts 3 float values as arguments and
	 *  initializes the component variables 
	 *  to the specified values.
	 * 
	 * @param a
	 * @param b
	 * @param c
	 */
	public Vec3D(float a, float b, float c)
	{
		x = a;
		y = b;
		z = c;
		
//		DEBUG
//		System.out.println("Vector values assigned: x=" + a + " y=" + b + " z=" + c);
	}
	
	/**
	 * Accepts an ArrayList of values for building a vector object.
	 * 
	 * @param vecValues The X Y and Z components of the vector in an ArrayList.
	 */
	public Vec3D(LinkedList<Float> vecValues) {
		
		Iterator<Float> vectItr = vecValues.iterator();
		x = vectItr.next();
		y = vectItr.next();
		z = vectItr.next();
		
//		DEBUG
//		System.out.println("Vector values assigned: x=" + x + " y=" + y + " z=" + z);
	}

	/**
	 * Accessor for the X-component of this vector.
	 * @return
	 */
	public float getX()
	{
		return x;
	}
	
	/**
	 * Accessor for the Y-component of this vector.
	 * @return
	 */
	public float getY()
	{
		return y;
	}
	
	/**
	 * Accessor for the Z-component of this vector.
	 * @return
	 */
	public float getZ()
	{
		return z;
	}
	
	/**
	 * Addition method add. Accepts an ArrayList<float>
	 * as an argument and performs vector addition on
	 * the current and passed ArrayList objects.
	 * Returns a calculated ArrayList<float>-based
	 * 3D vector.
	 */
	public Vec3D add(Vec3D v1)
	{
		return new Vec3D(x + v1.getX(), y + v1.getY(), z + v1.getZ());
	}
	
	/**
	 * Subtraction method sub. Accepts an ArrayList<float>
	 * as an argument and performs vector addition on
	 * the current and passed ArrayList objects, applying
	 * a negative to the component elements in v and v1.
	 * Returns a calculated ArrayList<float>-based
	 * 3D vector.
	 */
	public Vec3D sub(Vec3D v1)
	{
		return new Vec3D(x - v1.getX(), y - v1.getY(), z - v1.getZ());
	}
	
	/**
	 * Vector multiplication method mult. Accepts an
	 * ArrayList<float> as an argument and performs
	 * matrix cross-multiplication via cofactor
	 * expansion on the current and passed ArrayList
	 * objects.
	 * Returns a calculated ArrayList<float>-based
	 * 3D vector.
	 */
	public Vec3D cross(Vec3D v1)
	{
		float newX = y*v1.getZ()-z*v1.getY();
		float newY = z*v1.getX()-x*v1.getZ();
		float newZ = x*v1.getY()-y*v1.getX();
		
		return new Vec3D(newX, newY, newZ);
	}
	
	/**
	 * Vector dot product method dot. Accepts an
	 * ArrayList<float> as an argument and
	 * calculates a dot product from the current
	 * and passed ArrayList objects.
	 * Returns the dot product of this and the
	 * passed ArrayList<float>-based 3D vector
	 * object as a float value.
	 */
	public float dot(Vec3D v1)
	{
		return x*v1.getX() + y*v1.getY() + z*v1.getZ();
	}
	
	/**
	 * Overridden equals method. Nested if statements
	 *determine if a vector has the same values as
	 *another. Returns true if component values are
	 *equal, otherwise returns false.
	 */
	public boolean equals(Vec3D v1)
	{
		return ((x==v1.getX())&&(y==v1.getY())&&(z==v1.getZ()));
	}
	
	/**
	 * Scalar method scale. Scales a vector by a
	 * value s that is passed to scale. Multiplies
	 * all vector components by s.
	 * Returns a scaled ArrayList<float>-based
	 * 3D vector.
	 */
	public Vec3D scale(float s)
	{
		return new Vec3D(x*s, y*s, z*s);
	}
	
	/**
	 * Absolute value method abs. Calculates the
	 * magnitude of this vector ArrayList<float>
	 * object using the dot method then taking the
	 * square root of the result using the sqrt
	 * method contained in the Math class from
	 * java.lang.*.
	 * Returns the magnitude of this vector as a
	 * float value.
	 */
	public float abs()
	{
		return (float) Math.sqrt(dot(this));
	}
	
	/**
	 * Builds an array based on this vector.
	 * @return An array of floats in the form [X,Y,Z]
	 */
	public float[] asArray() {
		float[] components = {x,y,z};
		return components;
	}
	
	/**
	 * Static version of asArray that accepts a Vec3D and converts it to an array of floats.
	 * @param vector The vector you would like to convert to an array.
	 * @return A float array of the format [X,Y,Z]
	 */
	public static float[] asArray(Vec3D vector) {
		float[] components = {vector.getX(), vector.getY(), vector.getZ()};
		return components;
	}
	
	/**
	 * Computes the average vector of a collection of vectors.
	 * 
	 * @param vectors An ArrayList of vectors of which you want to find the average.
	 * @return A vector that is the average of all vectors in the collection of vectors.
	 */
	public static Vec3D average(LinkedList<Vec3D> vectors) {
		
		float xAvg = 0, yAvg = 0, zAvg = 0;
		
		for(Vec3D vector : vectors) {
			xAvg += vector.getX();
			yAvg += vector.getY();
			zAvg += vector.getZ();
		}
		
		xAvg /= vectors.size();
		yAvg /= vectors.size();
		zAvg /= vectors.size();
		
		return new Vec3D(xAvg, yAvg, zAvg);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 * 
	 * Overridden toString method giving the values
	 * of this ArrayList-based 3D vector object
	 * when called through a System.out.print*
	 * statement.
	 */
	public String toString()
	{
		return "Vector object " + this.hashCode() + ": ( " + x + ", " + y + ", " + z + " )";
	}
}