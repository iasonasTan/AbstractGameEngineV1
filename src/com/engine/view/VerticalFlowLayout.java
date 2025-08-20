package com.engine.view;

import java.awt.*;
import java.util.ArrayList;

public final class VerticalFlowLayout implements LayoutManager2 {
    private final ArrayList<Component> components = new ArrayList<>();
    public int vGap = 0; // vertical
    public int hGap = 0; // horizontal

    public VerticalFlowLayout() {
    }

    public VerticalFlowLayout(int hGap, int vGap) {
        this.hGap = hGap;
        this.vGap = vGap;
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        components.add(comp);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0;
    }

    @Override
    public void invalidateLayout(Container target) {

    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        components.add(comp);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        components.remove(comp);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Dimension out=new Dimension();
        for (Component c: components) {
            final int RIGHT = c.getPreferredSize().width+c.getX();
            if (out.width < RIGHT) {
                out.width = RIGHT;
            }

            final int DOWN = c.getPreferredSize().height+c.getY();
            if (out.height < DOWN) {
                out.height = DOWN;
            }
        }
        return out;
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return preferredLayoutSize(target);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    @Override
    public void layoutContainer(Container parent) {
        int x = hGap;
        int y = vGap;

        int maxWidth = 0;

        Dimension parentSize = parent.getPreferredSize();

        for (Component c: components) {
            if (!c.isVisible()) {
                continue;
            }

            Dimension componentSize = c.getPreferredSize();
            if (maxWidth < componentSize.width) {
                maxWidth = componentSize.width;
            }

            c.setBounds(x, y, componentSize.width, componentSize.height);

            y+=componentSize.height+vGap;

            if (y+componentSize.height > parentSize.height) {
                y = vGap;
                x+=hGap+maxWidth;
                maxWidth = vGap;
            }

        }

    }
}
