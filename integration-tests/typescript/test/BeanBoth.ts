import {Type, Expose} from 'class-transformer';


export class BeanBothBuilder implements BeanBothRequiresLastName, BeanBothRequiresSin, BeanBothNullable {
  _name: string;
  _lastName: string;
  _age: number;
  _sin: string;

public constructor() {}

public lastName(lastName : string) : BeanBothRequiresSin {
  this._lastName = lastName;
  return this;
}

public sin(sin : string) : BeanBothNullable {
  this._sin = sin;
  return this;
}

public name(name : string) : BeanBothNullable {
  this._name = name;
  return this;
}

public age(age : number) : BeanBothNullable {
  this._age = age;
  return this;
}

build() : BeanBoth {
 return new BeanBoth(this._name, this._lastName, this._age, this._sin);
}
}export interface BeanBothRequiresLastName {
  lastName(lastName : string) : BeanBothRequiresSin;
}

export interface BeanBothRequiresSin {
  sin(sin : string) : BeanBothNullable;
}
export interface BeanBothNullable {
  name(name : string) : BeanBothNullable;
  age(age : number) : BeanBothNullable;
  build() : BeanBoth;
}

export class BeanBoth {
  @Expose({ name: 'name' }) private _name: string;
  @Expose({ name: 'lastName' }) private _lastName: string;
  @Expose({ name: 'age' }) private _age: number;
  @Expose({ name: 'sin' }) private _sin: string;

static buildBeanBoth() : BeanBothRequiresLastName {
  return new BeanBothBuilder();
}

public constructor( name? : string, lastName? : string, age? : number, sin? : string) {
  this._name = name;
  this._lastName = lastName;
  this._age = age;
  this._sin = sin;
}

public get name() : string { return this._name; }
public get lastName() : string { return this._lastName; }
public get age() : number { return this._age; }
public get sin() : string { return this._sin; }
public withName(name : string) : BeanBoth {
  return new BeanBoth(name, this._lastName, this._age, this._sin);
}

public withLastName(lastName : string) : BeanBoth {
  return new BeanBoth(this._name, lastName, this._age, this._sin);
}

public withAge(age : number) : BeanBoth {
  return new BeanBoth(this._name, this._lastName, age, this._sin);
}

public withSin(sin : string) : BeanBoth {
  return new BeanBoth(this._name, this._lastName, this._age, sin);
}

}