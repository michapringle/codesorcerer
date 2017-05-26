package com.codesorcerer.abstracts;

import javax.lang.model.element.TypeElement;

public class Result<G extends AbstractSpell, Input, Output> {
    public TypeElement te;
    public G spell;
    public Input input;
    public Output output;
}