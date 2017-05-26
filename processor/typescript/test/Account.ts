import {Type, Expose} from 'class-transformer';


export class AccountBuilder implements AccountNullable {
  _name: string;
  _amount: number;
  _bDec: number;

private constructor() {}

public name(name : string) : AccountNullable {
  this._name = name;
  return this;
}

public amount(amount : number) : AccountNullable {
  this._amount = amount;
  return this;
}

public bDec(bDec : number) : AccountNullable {
  this._bDec = bDec;
  return this;
}

build() : Account {
 return new Account(this._name, this._amount, this._bDec);
}
}export interface AccountNullable {
  name(name : string) : AccountNullable;
  amount(amount : number) : AccountNullable;
  bDec(bDec : number) : AccountNullable;
  build() : Account;
}

export class Account {
  @Expose({ name: 'name' }) private _name: string;
  @Expose({ name: 'amount' }) private _amount: number;
  @Expose({ name: 'bDec' }) private _bDec: number;

static buildAccount() : AccountNullable {
  return new AccountBuilder();
}
static newAccount(name : string, amount : number, bDec : number) : Account {
  return new Account(name, amount, bDec);
}

public constructor( name : string, amount : number, bDec : number) {
  this._name = name;
  this._amount = amount;
  this._bDec = bDec;
}
public constructor() {}
public get name() : string { return this._name; }
public get amount() : number { return this._amount; }
public get bDec() : number { return this._bDec; }
public withName(name : string) : Account {
  return new Account(name, this._amount, this._bDec);
}

public withAmount(amount : number) : Account {
  return new Account(this._name, amount, this._bDec);
}

public withBDec(bDec : number) : Account {
  return new Account(this._name, this._amount, bDec);
}

}