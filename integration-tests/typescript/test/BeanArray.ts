import {Type, Expose} from 'class-transformer';


export class BeanArrayBuilder implements BeanArrayNullable {
  _names: string[];

public constructor() {}

public names(names : string[]) : BeanArrayNullable {
  this._names = names;
  return this;
}

build() : BeanArray {
 return new BeanArray(this._names);
}
}export interface BeanArrayNullable {
  names(names : string[]) : BeanArrayNullable;
  build() : BeanArray;
}

export class BeanArray {
  @Expose({ name: 'names' }) private _names: string[];

static buildBeanArray() : BeanArrayNullable {
  return new BeanArrayBuilder();
}
static newBeanArray(names : string[]) : BeanArray {
  return new BeanArray(names);
}

public constructor( names? : string[]) {
  this._names = names;
}

public get names() : string[] { return this._names; }
public withNames(names : string[]) : BeanArray {
  return new BeanArray(names);
}

}