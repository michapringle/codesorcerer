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
						"public abstract class EntityRef implements Serializable {",
						"	@Nonnull",
						"	private final String id;",
						"	public EntityRef( String id ) {",
						"		this.id = id;",
						"	}",
						"}"
				);

		JavaFileObject accountDef = JavaFileObjects
				.forSourceLines( "com.central1.lean.accounts.entities.AccountDef", "",
						"package com.central1.lean.accounts.entities;",
						"import com.beautifulbeanbuilder.LeanEntityRefTypescript;",
						"import com.beautifulbeanbuilder.BBBImmutable;",
						"import com.beautifulbeanbuilder.BBBTypescript;",
						"import com.beautifulbeanbuilder.BasicTypescriptMapping;",
						"import com.beautifulbeanbuilder.TypescriptMapping;",
						"import com.central1.leanannotations.LeanEntity;",
						"import javax.annotation.Nonnull; 			",
						"											",
						"@LeanEntity 								",
						"@LeanEntityRefTypescript								",
						"@TypescriptMapping(javaClassName = \"AccountRef\", typescriptClassName = \"AccountRef\", typescriptImportLocation = \"@c1/sdk\",\n"
								+ "        typescriptPackageName=\"@c1/sdk\", typescriptPackageVersion=\"^1.23\")											",
						"@BasicTypescriptMapping					",
						"@BBBImmutable								",
						"@BBBTypescript								",
						"public interface AccountDef { 						",
						"		@Nonnull								",
						"		AccountRef getRef(); 				",
						"		@Nonnull								",
						"		String getDescription(); 				",
						"	}											"
				);

		assertAbout(javaSources()).that( Arrays.asList( entityRef, accountDef))
				.withCompilerOptions( ImmutableList.of("-XprintRounds"))
				.processedWith( new EntityProcessor(), new BeanDefProcessor() )
				.compilesWithoutError();
	}
}
