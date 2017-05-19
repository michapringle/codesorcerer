package com.beautifulbeanbuilder;

import com.beautifulbeanbuilder.generators.beandef.BeanDefProcessor;
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

						"public interface Mapper<R extends EntityRef<T>, T> { ",
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

		JavaFileObject accountRef = JavaFileObjects
				.forSourceLines( "com.central1.lean.accounts.entities.AccountRef", "",
						"package com.central1.lean.accounts.entities;",
						"import com.central1.lean.entities.EntityRef;",
						"public class AccountRef extends EntityRef<Account> {",
						"		public AccountRef( final String id ) {",
						"			super( id, Account.class );						",
						"		}										",
						"}											"
				);

		JavaFileObject account = JavaFileObjects
				.forSourceLines( "com.central1.lean.accounts.entities.Account", "",
						"package com.central1.lean.accounts.entities;",
						"	public class Account { 						",
						"		private AccountRef ref;					",
						"		public Account( AccountRef ref ) {		",
						" 			this.ref = ref;						",
						"		}										",
						"		public AccountRef getRef() { 			",
						"  			return ref;							",
						"		}										",
						"	}											"
				);

		JavaFileObject accountGroupRef = JavaFileObjects
				.forSourceLines( "com.central1.lean.accounts.entities.AccountGroupRef", "",
						"package com.central1.lean.accounts.entities;",
						"import com.central1.lean.entities.EntityRef;",
						"public class AccountGroupRef extends EntityRef<AccountGroup> {",
						"		public AccountGroupRef( final String id ) {",
						"			super( id, AccountGroup.class );						",
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
						"		public Single<Object> updateAccount( @Nonnull AccountRef ref, @Nonnull String newName, @Nonnull boolean test )",
						"		{																						",
						"			return Single.just( new Object() );									",
						"		}																							",
						"     							                                               ",
						"}                                                                                                                                       "
				);

		assertAbout(javaSources()).that( Arrays.asList(mapper, entityRef, account, accountRef, accountGroup, accountGroupRef, usecase))
				.withCompilerOptions( ImmutableList.of("-XprintRounds"))
				.processedWith(new UsecaseProcessor(), new BeanDefProcessor() )
				.compilesWithoutError();
	}
}
