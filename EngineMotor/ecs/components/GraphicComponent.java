package ecs.components;

import ecs.Component;
import graphics.*; // access Model, RenderMode, etc.

public class GraphicComponent implements Component {
    public Model model;
    public RenderMode mode = RenderMode.TRIANGLES;
    public float lineWidth = 1.0f;
    public float pointSize = 1.0f;
}
