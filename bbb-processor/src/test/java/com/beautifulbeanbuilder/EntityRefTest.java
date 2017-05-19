package com.beautifulbeanbuilder;

import com.beautifulbeanbuilder.generators.beandef.BeanDefProcessor;
import com.beautifulbeanbuilder.generators.entity.EntityProcessor;
import com.google.common.collect.ImmutableList;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;
import java.util.Arrays;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

@RunWith(JUnit4.class)
public class EntityRefTest
{
	@Test
	public void simple() throws Exception
	{
		JavaFileObject entityRef = JavaFileObjects
				.forSourceLines( "com.central1.lean.entities.EntityRef", "",
						"package com.central1.lean.entities;",
						"		",
						"import javax.annotation.Nonnull;",
						"import java.io.Serializable;",
						"  ",
						"public abstract class EntityRef<T> implements Serializable {",
						"	@Nonnull",
						"	private final String id;",
						"	@Nonnull",
						"	private final Class<T> entityType;",
						"	public EntityRef( String id, Class<T> entityType ) {",
						"		this.id = id;",
						"		this.entityType = entityType;",
						"	}",
						"}"
				);

		JavaFileObject accountDef = JavaFileObjects
				.forSourceLines( "com.central1.lean.accounts.entities.AccountDef", "",
						"package com.central1.lean.accounts.entities;",
						"import com.beautifulbeanbuilder.BBBImmutable;",
						"import com.central1.leanannotations.LeanEntity;",
						"import javax.annotation.Nonnull; 			",
						"											",
						"@BBBImmutable								",
						"@LeanEntity 								",
						"public interface AccountDef { 						",
						"		@Nonnull								",
						"		AccountRef getRef(); 				",
						"		@Nonnull								",
						"		String getDescription(); 				",
						"	}											"
				);

		assertAbout(javaSources()).that( Arrays.asList( entityRef, accountDef))
				.withCompilerOptions( ImmutableList.of("-XprintRounds"))
				.processedWith( new BeanDefProcessor(), new EntityProcessor() )
				.compilesWithoutError();
	}
}
