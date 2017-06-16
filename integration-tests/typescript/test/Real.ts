import {Type, Expose} from 'class-transformer';


export class RealBuilder implements RealNullable {
  _thing: string;

public constructor() {}

public thing(thing : string) : RealNullable {
  this._thing = thing;
  return this;
}

build() : Real {
 return new Real(this._thing);
}
}export interface RealNullable {
  thing(thing : string) : RealNullable;
  build() : Real;
}

export class Real {
  @Expose({ name: 'thing' }) private _thing: string;

static buildReal() : RealNullable {
  return new RealBuilder();
}
static newReal(thing : string) : Real {
  return new Real(thing);
}

public constructor( thing? : string) {
  this._thing = thing;
}

public get thing() : string { return this._thing; }
public withThing(thing : string) : Real {
  return new Real(thing);
}

}