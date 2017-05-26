import {Type, Expose} from 'class-transformer';

import {BeanChild} from 'test.BeanChild';

export class BeanCollBuilder implements BeanCollNullable {
  _names: Array<string>;
  _ages: Set<number>;
  _nameToAge: Map<string,number>;

private constructor() {}

public names(names : Array<string>) : BeanCollNullable {
  this._names = names;
  return this;
}

public ages(ages : Set<number>) : BeanCollNullable {
  this._ages = ages;
  return this;
}

public nameToAge(nameToAge : Map<string,number>) : BeanCollNullable {
  this._nameToAge = nameToAge;
  return this;
}

build() : BeanColl {
 return new BeanColl(this._names, this._ages, this._nameToAge);
}
}export interface BeanCollNullable {
  names(names : Array<string>) : BeanCollNullable;
  ages(ages : Set<number>) : BeanCollNullable;
  nameToAge(nameToAge : Map<string,number>) : BeanCollNullable;
  build() : BeanColl;
}

export class BeanColl {
@Type(() => Array<string>)  @Expose({ name: 'names' }) private _names: Array<string>;
@Type(() => Set<number>)  @Expose({ name: 'ages' }) private _ages: Set<number>;
@Type(() => Map<string,number>)  @Expose({ name: 'nameToAge' }) private _nameToAge: Map<string,number>;

static buildBeanColl() : BeanCollNullable {
  return new BeanCollBuilder();
}
static newBeanColl(names : Array<string>, ages : Set<number>, nameToAge : Map<string,number>) : BeanColl {
  return new BeanColl(names, ages, nameToAge);
}

public constructor( names : Array<string>, ages : Set<number>, nameToAge : Map<string,number>) {
  this._names = names;
  this._ages = ages;
  this._nameToAge = nameToAge;
}
public constructor() {}
public get names() : Array<string> { return this._names; }
public get ages() : Set<number> { return this._ages; }
public get nameToAge() : Map<string,number> { return this._nameToAge; }
public withNames(names : Array<string>) : BeanColl {
  return new BeanColl(names, this._ages, this._nameToAge);
}

public withAges(ages : Set<number>) : BeanColl {
  return new BeanColl(this._names, ages, this._nameToAge);
}

public withNameToAge(nameToAge : Map<string,number>) : BeanColl {
  return new BeanColl(this._names, this._ages, nameToAge);
}

}