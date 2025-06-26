/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services.dto.google.places;

public class Geometry {
    private Viewport rectangle;
    private Circle circle;

    public Geometry() {
    }

    public Geometry(Viewport rectangle) {
        this.rectangle = rectangle;
    }

    public Geometry(Circle circle) {
        this.circle = circle;
    }

    public Viewport getRectangle() {
        return rectangle;
    }

    public void setRectangle(Viewport rectangle) {
        if (circle != null && rectangle != null)
            throw new IllegalArgumentException("Cannot set both rectangle and circle for Geometry");

        this.rectangle = rectangle;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        if (circle != null && rectangle != null)
            throw new IllegalArgumentException("Cannot set both rectangle and circle for Geometry");

        this.circle = circle;
    }
}