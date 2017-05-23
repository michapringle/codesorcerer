package com.beautifulbeanbuilder;

import com.beautifulbeanbuilder.generators.beandef.BeanDefProcessor;
import com.beautifulbeanbuilder.generators.entity.EntityProcessor;
import com.beautifulbeanbuilder.generators.usecase.UsecaseProcessor;
import com.central1.leanannotations.LeanUsecase;
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
public class UsecaseTest
{

	@Test
	public void simple() throws Exception
	{
		JavaFileObject mapper = JavaFileObjects
				.forSourceLines( "com.central1.lean.mapping.Mapper", "",
						"package com.central1.lean.mapping;",
						" ",
						"import com.central1.lean.entities.EntityRef;",
						"import io.reactivex.Observable;",
						"import javax.annotation.Nonnull;",

						"public interface Mapper<R extends EntityRef, T> { ",
						"	Observable<T> getEntity( @Nonnull R ref );",
						"}"
				);

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

		JavaFileObject c1Entity = JavaFileObjects
				.forSourceLines( "com.central1.lean.entities.C1Entity", "",
						"package com.central1.lean.entities;\n"
								+ "\n"
								+ "import com.beautifulbeanbuilder.BBBImmutable;\n"
								+ "import com.beautifulbeanbuilder.BBBJson;\n"
								+ "import com.beautifulbeanbuilder.LeanEntityRefTypescript;\n"
								+ "import com.central1.leanannotations.LeanEntity;\n"
								+ "\n"
								+ "@LeanEntity\n"
								+ "@LeanEntityRefTypescript\n"
								+ "@BBBImmutable\n"
								+ "@BBBJson\n"
								+ "public @interface C1Entity\n"
								+ "{\n"
								+ "}"
				);

		JavaFileObject account = JavaFileObjects
				.forSourceLines( "com.central1.lean.accounts.entities.AccountDef", "",
						"package com.central1.lean.accounts.entities;",
//						"import com.beautifulbeanbuilder.BBBImmutable;\n",
//						"import com.beautifulbeanbuilder.BBBJson;\n",
//						"import com.beautifulbeanbuilder.LeanEntityRefTypescript;\n",
//						"import com.central1.leanannotations.LeanEntity;",
						"import com.central1.lean.entities.C1Entity;",
						"import javax.annotation.Nonnull;\n",
						"\n",
						"@C1Entity\n",
//						"@LeanEntityRefTypescript\n",
//						"@BBBJson\n",
//						"@BBBImmutable",
						"	public interface AccountDef { 						",
						"		@Nonnull\n",
						"		AccountRef getRef();\n",
//						"		private AccountRef ref;					",
//						"		public Account( AccountRef ref ) {		",
//						" 			this.ref = ref;						",
//						"		}										",
//						"		public AccountRef getRef() { 			",
//						"  			return ref;							",
//						"		}										",
						"	}											"
				);

		JavaFileObject accountGroupRef = JavaFileObjects
				.forSourceLines( "com.central1.lean.accounts.entities.AccountGroupRef", "",
						"package com.central1.lean.accounts.entities;",
						"import com.central1.lean.entities.EntityRef;",
						"public class AccountGroupRef extends EntityRef {",
						"		public AccountGroupRef( final String id ) {",
						"			super( id );						",
						"		}										",
						"}											"
				);

		JavaFileObject accountGroup = JavaFileObjects
				.forSourceLines( "com.central1.lean.accounts.entities.AccountGroup", "",
						"package com.central1.lean.accounts.entities;",
						"	public class AccountGroup { 						",
						"		private AccountGroupRef ref;					",
						"		public AccountGroup( AccountGroupRef ref ) {		",
						" 			this.ref = ref;						",
						"		}										",
						"		public AccountGroupRef getRef() { 			",
						"  			return ref;							",
						"		}										",
						"	}											"
				);

		JavaFileObject usecase = JavaFileObjects
				.forSourceLines( "com.central1.lean.accounts.usecases.GetAccounts", "",
						"package com.central1.lean.accounts.usecases;                                                                                                  ",
						"                                                                                                   ",
						"import com.central1.lean.accounts.entities.AccountGroup;",
						"import com.central1.lean.accounts.entities.AccountGroupRef;",
						"import com.central1.lean.accounts.entities.Account;",
						"import com.central1.lean.accounts.entities.AccountRef;",
						"import io.reactivex.Observable;                                                                                                          ",
						"import io.reactivex.Single;                                                                                                          ",
						"import javax.annotation.Nonnull;                                                                                               ",
						"import java.util.List;                                                                                                          ",
						"import java.util.ArrayList;                                                                                                          ",
						"import java.io.Serializable;				",
						"import " + LeanUsecase.class.getName() + ";   ",
						"import " + Expose.class.getName() + ";   ",
						"                                                                                                                                                    ",
						"   @LeanUsecase                                                      ",
						"   public class GetAccounts {                                                  ",
						"																			",
						"		@Expose																",
						"		public Observable<List<AccountRef>> getAccounts( @Nonnull AccountGroupRef groupRef )",
						"		{																						",
						"			return Observable.just( new ArrayList<AccountRef>() );									",
						"		}																							",
						"		@Expose																",
						"		public Observable<List<AccountGroupRef>> getAccountGroups()",
						"		{																						",
						"			return Observable.just( new ArrayList<AccountGroupRef>() );									",
						"		}																							",
						"		@Expose																",
						"		public Single<Object> updateAccount( @Nonnull AccountRef ref, @Nonnull String newName, @Nonnull boolean test )",
						"		{																						",
						"			return Single.just( new Object() );									",
						"		}																							",
						"     							                                               ",
						"}                                                                                                                                       "
				);

		assertAbout(javaSources()).that( Arrays.asList(mapper, entityRef, c1Entity, account, accountGroup, accountGroupRef, usecase))
				.withCompilerOptions( ImmutableList.of("-XprintRounds"))
				.processedWith( new EntityProcessor(), new BeanDefProcessor(), new UsecaseProcessor() )
				.compilesWithoutError();
	}
}
