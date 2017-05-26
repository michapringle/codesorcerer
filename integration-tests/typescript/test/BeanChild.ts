import {Type, Expose} from 'class-transformer';

import {BeanChild} from 'test.BeanChild';

export class BeanChildBuilder implements BeanChildNullable {
  _name: string;

private constructor() {}

public name(name : string) : BeanChildNullable {
  this._name = name;
  return this;
}

build() : BeanChild {
 return new BeanChild(this._name);
}
}export interface BeanChildNullable {
  name(name : string) : BeanChildNullable;
  build() : BeanChild;
}

export class BeanChild {
  @Expose({ name: 'name' }) private _name: string;

static buildBeanChild() : BeanChildNullable {
  return new BeanChildBuilder();
}
static newBeanChild(name : string) : BeanChild {
  return new BeanChild(name);
}

public constructor( name : string) {
  this._name = name;
}
public constructor() {}
public get name() : string { return this._name; }
public withName(name : string) : BeanChild {
  return new BeanChild(name);
}

}