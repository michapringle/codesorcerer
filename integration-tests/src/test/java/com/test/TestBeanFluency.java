package com.test;

import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.union;

import java.lang.reflect.Method;
import java.util.Set;

import com.test.template.bnn_sbn.Simple3;
import com.test.template.bnn_sbnn.Complex2;
import com.test.template.bnn_sbnn.Simple2;
import com.test.template.bn_sbn.Complex5;
import com.test.template.bn_sbn.Simple5;
import com.test.template.bn_sbnn.Complex4;
import com.test.template.bn_sbnn.Simple4;
import org.junit.Test;

import com.test.template.Complex;
import com.test.template.Simple;
import com.test.template.Thing;
import com.test.template.bnn_sbn.Complex3;


/**
 * Tests the beans for fluency, to make sure they can be called as expected.
 * <p/>
 * Created by mpringle on August 06, 2014.
 */
public class TestBeanFluency
{

	@Test
	public void testSingleNonNull()
	{
		Complex3.
		buildComplex3().
		newSimple3().
		name("").
		done().
		build();
	}
		
		
	private void ensureOnly( Class<?> c, String... expectedArray )
	{
		final Set<String> expected = newHashSet( expectedArray );

		final Set<String> actual = newHashSet();
		for ( Method m : c.getMethods() )
		{
			actual.add( m.getName() );
		}

		final Set<String> difference = union( difference( actual, expected ), difference( expected, actual ) );

		if ( !difference.isEmpty() )
		{
			throw new RuntimeException( "Not expecting methods " + difference + " to exist! Only " + expected );
		}
	}

	@Test
	public void testOnlyMinimalAPIExists()
	{
		ensureOnly( Complex.BeanRequires0.class, "title1" );
		ensureOnly( Complex.BeanRequires1.class, "title2" );
		ensureOnly( Complex.BeanRequires2.class, "title3" );

		ensureOnly( Complex.BeanBuildable.class, "simple1", "simple2", "simple21", "thing", "title4", "title5", "title6", "newSimple1", "newSimple21", "newThing",
				"newSimple2", "build" );
		
		ensureOnly( Complex.SubBeanBuildable.class, "simple1", "simple2", "simple21", "thing", "title4", "title5", "title6", "newSimple1", "newSimple21", "newThing",
				"newSimple2", "done" );
		
		ensureOnly( Complex.BeanUpdateable.class, "simple1", "simple2", "simple21", "thing", "title1", "title2", "title3", "title4", "title5", "title6",
				"newSimple1", "newThing", "newSimple2", "newSimple21",
				"getSimple1", "getThing", "getSimple2", "getSimple21", "build" );
		
		ensureOnly( Complex.SubBeanUpdatable.class, "simple1", "simple2", "simple21", "thing", "title1", "title2", "title3", "title4", "title5", "title6",
				"newSimple1", "newThing", "newSimple2", "newSimple21",
				"getSimple1", "getThing", "getSimple2", "getSimple21", "done" );

	}

	/**
	 * This test case follows all possible valid paths of the fluent interface
	 * for the Complex object. If the code compiles, then it works, if not, then
	 * it is broken.
	 * <p/>
	 * This is a positive test only, it does not validate the invalid paths.
	 */
	@Test
	public void testComplexAllValidFluentPaths()
	{
		final Complex.BeanRequires0 t1 = Complex.buildComplex();
		final Complex.BeanRequires1 t2 = t1.title1( "" );
		final Complex.BeanRequires2 t3 = t2.title2( "" );
		final Complex.BeanBuildable te = t3.title3( "" );

		// test all the end paths available
		final Complex.BeanBuildable te1 = te.simple1( null );
		final Complex.BeanBuildable te2 = te.simple2( null );
		final Complex.BeanBuildable te3 = te.thing( null );
		final Complex.BeanBuildable te4 = te.title4( "" );
		final Complex.BeanBuildable te5 = te.title5( "" );
		final Complex.BeanBuildable te6 = te.title6( "" );

		// doing the same thing again is allowed, but silly
		final Complex.BeanBuildable te7 = te1.simple1( null );
		final Complex.BeanBuildable te8 = te2.simple2( null );
		final Complex.BeanBuildable te9 = te3.thing( null );
		final Complex.BeanBuildable te10 = te4.title4( "" );
		final Complex.BeanBuildable te11 = te5.title5( "" );
		final Complex.BeanBuildable te12 = te6.title6( "" );

		// create the aggregate (composed) BB's
		final Simple.SubBeanRequires0<Complex.BeanBuildable> simple1 = te.newSimple1();
		final Simple.SubBeanRequires0<Complex.BeanBuildable> simple2 = te.newSimple2();
		final Thing.SubBeanRequires0<Complex.BeanBuildable> thing = te.newThing();

		// the allowed path on the first simple is enough to test
		final Complex.BeanBuildable sd1a = simple1.done();
		final Simple.SubBeanBuildable<Complex.BeanBuildable> sd1b = simple1.name( "" );
		final Simple.SubBeanBuildable<Complex.BeanBuildable> sd1c = sd1b.name( "" );
		final Complex.BeanBuildable sd1d = sd1c.done();
		final Complex complex1 = sd1a.build();
		final Complex complex2 = te.build();

		// Updater chain...
		final Complex.BeanUpdateable u1 = complex1.update();
		final Complex.BeanUpdateable u2 = u1.title1( "" );
		final Simple.SubBeanUpdatable<Complex.BeanUpdateable> u3 = u2.getSimple1();
		final Simple.SubBeanUpdatable<Complex.BeanUpdateable> u4 = u3.name( "" );
		final Complex.BeanUpdateable u5 = u4.done();
		final com.test.template.Thing.SubBeanUpdatable<Complex.BeanUpdateable> u6 = u5.getThing();
		final com.test.template.Thing.SubBeanUpdatable<Complex.BeanUpdateable> u7 = u6.that( "" );
		final Complex.BeanUpdateable u8 = u7.done();
		final Complex.BeanUpdateable u9 = u8.title6( "" );
		final Complex u10 = u9.build();
	}

	@Test
	public void testSubBeanChain()
	{
		final Complex2.BeanRequires0 c2s = Complex2.buildComplex2();
		final Simple2.SubBeanRequires0<Complex2.BeanBuildable> s21 = c2s.newSimple2();
		final Simple2.SubBeanBuildable<Complex2.BeanBuildable> s22 = s21.name("");
		final Complex2.BeanBuildable s2e = s22.done();
		final Complex2 c2e = s2e.build();

		final Complex3.BeanRequires0 c3s = Complex3.buildComplex3();
		final Simple3.SubBeanRequires0<Complex3.BeanBuildable> s31 = c3s.newSimple3();
		final Simple3.SubBeanBuildable<Complex3.BeanBuildable> s32 = s31.name("");
		final Complex3.BeanBuildable s3e = s32.done();
		final Complex3 c3e = s3e.build();

		final Complex4.BeanBuildable c4s = Complex4.buildComplex4();
		final Simple4.SubBeanRequires0<Complex4.BeanBuildable> s41 = c4s.newSimple4();
		final Simple4.SubBeanBuildable<Complex4.BeanBuildable> s42 = s41.name("");
		final Complex4.BeanBuildable s4e = s42.done();
		final Complex4 c4e = s4e.build();

		final Complex5.BeanBuildable c5s = Complex5.buildComplex5();
		final Simple5.SubBeanRequires0<Complex5.BeanBuildable> s51 = c5s.newSimple5();
		final Simple5.SubBeanBuildable<Complex5.BeanBuildable> s52 = s51.name("");
		final Complex5.BeanBuildable s5e = s52.done();
		final Complex5 c5e = s5e.build();
	}
}