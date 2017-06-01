import {Type, Expose} from 'class-transformer';


export class BeanSimple1Builder implements BeanSimple1Nullable {
  _name: string;
  _age: number;

public constructor() {}

public name(name : string) : BeanSimple1Nullable {
  this._name = name;
  return this;
}

public age(age : number) : BeanSimple1Nullable {
  this._age = age;
  return this;
}

build() : BeanSimple1 {
 return new BeanSimple1(this._name, this._age);
}
}export interface BeanSimple1Nullable {
  name(name : string) : BeanSimple1Nullable;
  age(age : number) : BeanSimple1Nullable;
  build() : BeanSimple1;
}

export class BeanSimple1 {
  @Expose({ name: 'name' }) private _name: string;
  @Expose({ name: 'age' }) private _age: number;

static buildBeanSimple1() : BeanSimple1Nullable {
  return new BeanSimple1Builder();
}
static newBeanSimple1(name : string, age : number) : BeanSimple1 {
  return new BeanSimple1(name, age);
}

public constructor( name? : string, age? : number) {
  this._name = name;
  this._age = age;
}

public get name() : string { return this._name; }
public get age() : number { return this._age; }
public withName(name : string) : BeanSimple1 {
  return new BeanSimple1(name, this._age);
}

public withAge(age : number) : BeanSimple1 {
  return new BeanSimple1(this._name, age);
}

}