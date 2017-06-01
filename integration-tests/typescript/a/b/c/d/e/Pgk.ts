import {Type, Expose} from 'class-transformer';


export class PgkBuilder implements PgkNullable {
  _name: string;

private constructor() {}

public name(name : string) : PgkNullable {
  this._name = name;
  return this;
}

build() : Pgk {
 return new Pgk(this._name);
}
}export interface PgkNullable {
  name(name : string) : PgkNullable;
  build() : Pgk;
}

export class Pgk {
  @Expose({ name: 'name' }) private _name: string;

static buildPgk() : PgkNullable {
  return new PgkBuilder();
}
static newPgk(name : string) : Pgk {
  return new Pgk(name);
}

public constructor( name : string) {
  this._name = name;
}
public constructor() {}
public get name() : string { return this._name; }
public withName(name : string) : Pgk {
  return new Pgk(name);
}

}