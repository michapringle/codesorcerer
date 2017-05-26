import {Type, Expose} from 'class-transformer';

import {BeanChild} from 'test.BeanChild';

export class BeanParentBuilder implements BeanParentNullable {
  _child: BeanChild;

private constructor() {}

public child(child : BeanChild) : BeanParentNullable {
  this._child = child;
  return this;
}

build() : BeanParent {
 return new BeanParent(this._child);
}
}export interface BeanParentNullable {
  child(child : BeanChild) : BeanParentNullable;
  build() : BeanParent;
}

export class BeanParent {
@Type(() => BeanChild)  @Expose({ name: 'child' }) private _child: BeanChild;

static buildBeanParent() : BeanParentNullable {
  return new BeanParentBuilder();
}
static newBeanParent(child : BeanChild) : BeanParent {
  return new BeanParent(child);
}

public constructor( child : BeanChild) {
  this._child = child;
}
public constructor() {}
public get child() : BeanChild { return this._child; }
public withChild(child : BeanChild) : BeanParent {
  return new BeanParent(child);
}

}