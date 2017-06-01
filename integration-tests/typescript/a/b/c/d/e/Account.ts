import {Type, Expose} from 'class-transformer';


export class AccountBuilder implements AccountNullable {
  _name: string;

public constructor() {}

public name(name : string) : AccountNullable {
  this._name = name;
  return this;
}

build() : Account {
 return new Account(this._name);
}
}export interface AccountNullable {
  name(name : string) : AccountNullable;
  build() : Account;
}

export class Account {
  @Expose({ name: 'name' }) private _name: string;

static buildAccount() : AccountNullable {
  return new AccountBuilder();
}
static newAccount(name : string) : Account {
  return new Account(name);
}

public constructor( name? : string) {
  this._name = name;
}

public get name() : string { return this._name; }
public withName(name : string) : Account {
  return new Account(name);
}

}