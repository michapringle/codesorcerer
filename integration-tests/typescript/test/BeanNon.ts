import {Type, Expose} from 'class-transformer';

import {BeanChild} from 'test.BeanChild';

export class BeanNonBuilder implements BeanNonRequiresLastName, BeanNonRequiresSin, BeanNonNullable {
  _lastName: string;
  _sin: string;

private constructor() {}

public lastName(lastName : string) : BeanNonRequiresSin {
  this._lastName = lastName;
  return this;
}

public sin(sin : string) : BeanNonNullable {
  this._sin = sin;
  return this;
}

build() : BeanNon {
 return new BeanNon(this._lastName, this._sin);
}
}export interface BeanNonRequiresLastName {
  lastName(lastName : string) : BeanNonRequiresSin;
}

export interface BeanNonRequiresSin {
  sin(sin : string) : BeanNonNullable;
}
export interface BeanNonNullable {
  build() : BeanNon;
}

export class BeanNon {
  @Expose({ name: 'lastName' }) private _lastName: string;
  @Expose({ name: 'sin' }) private _sin: string;

static buildBeanNon() : BeanNonRequiresLastName {
  return new BeanNonBuilder();
}
static newBeanNon(lastName : string, sin : string) : BeanNon {
  return new BeanNon(lastName, sin);
}

public constructor( lastName : string, sin : string) {
  this._lastName = lastName;
  this._sin = sin;
}
public constructor() {}
public get lastName() : string { return this._lastName; }
public get sin() : string { return this._sin; }
public withLastName(lastName : string) : BeanNon {
  return new BeanNon(lastName, this._sin);
}

public withSin(sin : string) : BeanNon {
  return new BeanNon(this._lastName, sin);
}

}