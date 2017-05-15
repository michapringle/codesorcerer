package com.beautifulbeanbuilder;

import com.central1.leanannotations.LeanUsecase;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

@RunWith(JUnit4.class)
public class UsecaseTest
{

	@Test
	public void simple() throws Exception
	{

		JavaFileObject o1 = JavaFileObjects
				.forSourceLines( "test.Test", "",
						"package com.central1.lean.accounts.usecases;                                                                                                  ",
						"                                                                                                   ",
						"import io.reactivex.Observable;                                                                                                          ",
						"import io.reactivex.Single;                                                                                                          ",
						"import javax.annotation.Nonnull;                                                                                               ",
						"import java.util.List;                                                                                                          ",
						"import java.util.ArrayList;                                                                                                          ",
						"import java.io.Serializable;				",
						"import " + LeanUsecase.class.getName() + ";   ",
						"import " + Expose.class.getName() + ";   ",
						"                                                                                                                                                    ",
						"                                                                                                                                                    ",
						"public class Test {                                                     ",
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
						"		public Observable<AccountRef> getAccount( @Nonnull AccountRef ref )",
						"		{																						",
						"			return Observable.empty();									",
						"		}																							",
						"		@Expose																",
						"		public Single<Object> updateAccount( @Nonnull AccountRef ref, @Nonnull String newName, @Nonnull boolean test )",
						"		{																						",
						"			return Single.just( new Object() );									",
						"		}																							",
						"     							                                               ",
						"   }                                                                          ",
						"	public class AccountGroupRef { 			",
						"		private final String id;		",
						"		public AccountGroupRef( final String id ) {",
						"			this.id = id;						",
						"		}										",
						"	}											",
						"	public class AccountRef { 			",
						"		private final String id;		",
						"		public AccountRef( final String id ) {",
						"			this.id = id;						",
						"		}										",
						"	}											",
						"	public class Account { 						",
						"		private AccountRef ref;					",
						"		public Account( AccountRef ref ) {		",
						" 			this.ref = ref;						",
						"		}										",
						"		public AccountRef getRef() { 			",
						"  			return ref;							",
						"		}										",
						"	}											",
						"	public class EntityRef<T> implements Serializable { ",
						"		@Nonnull private final String id; 				",
						"		@Nonnull private final Class<T> entityType;			",
						"		EntityRef( String id, Class<T> entityType ) {		",
						"			this.id = id;									",
						"			this.entityType = entityType;					",
						"		}													",
						"	}											",
						"	public interface Mapper<T, R extends EntityRef<T>> { 						",
						"		Observable<T> getEntity( @Nonnull R ref );				",
						"	}",
						"	public class AccountMapper<Account, AccountRef> { 						",
						"		public Observable<Account> getEntity( @Nonnull AccountRef ref) {				",
						"			return Observable.empty(); 											",
						"		}																	",
						"	}",
						"}                                                                                                                                       "
				);

//		assertAbout(javaSources()).that( Arrays.asList(o1))
//				.withCompilerOptions( ImmutableList.of("-XprintRounds"))
//				.processedWith(new UsecaseProcessor() )
//				.compilesWithoutError();
	}
}
