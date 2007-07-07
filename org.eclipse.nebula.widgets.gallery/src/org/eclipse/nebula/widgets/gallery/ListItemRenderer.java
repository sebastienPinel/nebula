/*******************************************************************************
 * Copyright (c) 2006-2007 Nicolas Richeton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors :
 *    Nicolas Richeton (nicolas.richeton@gmail.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.gallery;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Item drawing with a list style :<br/> Image on the left, text and
 * description on the right.<br/>
 * 
 * Best with bigger width than height.
 * <p>
 * NOTE: THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT. THIS IS A
 * PRE-RELEASE ALPHA VERSION. USERS SHOULD EXPECT API CHANGES IN FUTURE
 * VERSIONS.
 * </p>
 * 
 * @author Nicolas Richeton (nicolas.richeton@gmail.com)
 * 
 */

public class ListItemRenderer extends AbstractGalleryItemRenderer {

	Vector dropShadowsColors = new Vector();

	boolean dropShadows = false;

	int dropShadowsSize = 5;

	int dropShadowsAlphaStep = 20;

	Color selectionBackgroundColor;

	Color selectionForegroundColor;

	Color foregroundColor;

	Color backgroundColor;

	Color descriptionColor;

	Font textFont;

	Font descriptionFont;

	boolean showLabels = true;

	public boolean isShowLabels() {
		return showLabels;
	}

	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
	}

	public ListItemRenderer() {
		foregroundColor = Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		backgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		selectionBackgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION);
		this.selectionForegroundColor = foregroundColor;
		descriptionColor = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);

		textFont = Display.getDefault().getSystemFont();
		descriptionFont = Display.getDefault().getSystemFont();
	}

	public void draw(GC gc, GalleryItem item, int index, int x, int y, int width, int height) {

		Image itemImage = item.getImage();

		int useableHeight = height;

		int imageWidth = 0;
		int imageHeight = 0;
		int xShift = 0;
		int yShift = 0;
		Point size = null;

		if (itemImage != null) {
			Rectangle itemImageBounds = itemImage.getBounds();
			imageWidth = itemImageBounds.width;
			imageHeight = itemImageBounds.height;

			size = getBestSize(imageWidth, imageHeight, useableHeight - 4 - this.dropShadowsSize, useableHeight - 4 - this.dropShadowsSize);

			xShift = ((useableHeight - size.x) >> 1) + 2;
			yShift = (useableHeight - size.y) >> 1;

			if (dropShadows) {
				Color c = null;
				for (int i = this.dropShadowsSize - 1; i >= 0; i--) {
					c = (Color) dropShadowsColors.get(i);
					gc.setForeground(c);

					gc.drawLine(x + useableHeight + i - xShift - 1, y + dropShadowsSize + yShift, x + useableHeight + i - xShift - 1, y + useableHeight + i
							- yShift);
					gc.drawLine(x + xShift + dropShadowsSize, y + useableHeight + i - yShift - 1, x + useableHeight + i - xShift, y - 1 + useableHeight + i
							- yShift);
				}
			}
		}

		// Draw selection background (rounded rectangles)
		if (selected) {
			gc.setBackground(selectionBackgroundColor);
			gc.setForeground(selectionBackgroundColor);
			gc.fillRoundRectangle(x, y, width, useableHeight, 15, 15);
		}

		if (itemImage != null) {
			if (size.x > 0 && size.y > 0) {
				gc.drawImage(itemImage, 0, 0, imageWidth, imageHeight, x + xShift, y + yShift, size.x, size.y);
			}
		}

		if (item.getText() != null && showLabels) {

			// Calculate font height (text and description)
			gc.setFont(textFont);
			String text = RendererHelper.createLabel(item.getText(), gc, width - useableHeight - 10);
			int textFontHeight = gc.getFontMetrics().getHeight();

			String description = null;
			int descriptionFontHeight = 0;
			if (item.getDescription() != null) {
				gc.setFont(descriptionFont);
				description = RendererHelper.createLabel(item.getDescription(), gc, width - useableHeight - 10);
				descriptionFontHeight = gc.getFontMetrics().getHeight();
			}

			boolean displayText = false;
			boolean displayDescription = false;
			int remainingHeight = height - 2 - textFontHeight;
			if (remainingHeight > 0)
				displayText = true;
			remainingHeight -= descriptionFontHeight;
			if (remainingHeight > 0)
				displayDescription = true;

			// Background color
			gc.setBackground(selected ? selectionBackgroundColor : backgroundColor);

			// Draw text
			if (displayText) {
				int transY = (height - textFontHeight - 2);
				if (displayDescription)
					transY -= descriptionFontHeight;
				transY = transY >> 1;
				gc.setForeground(selected ? this.selectionForegroundColor : this.foregroundColor);
				gc.setFont(textFont);
				gc.drawText(text, x + useableHeight + 5, y + transY, true);
			}
			// Draw description
			if (description != null && displayDescription) {
				gc.setForeground(this.descriptionColor);
				gc.setFont(descriptionFont);
				gc.drawText(description, x + useableHeight + 5, y + ((height - descriptionFontHeight - textFontHeight - 2) >> 1) + textFontHeight + 1, true);
			}
		}
	}

	public void setDropShadowsSize(int dropShadowsSize) {
		this.dropShadowsSize = dropShadowsSize;
		this.dropShadowsAlphaStep = (dropShadowsSize == 0) ? 0 : (200 / dropShadowsSize);

		freeDropShadowsColors();
		createColors();
		// TODO: force redraw

	}

	private void createColors() {
		if (dropShadowsSize > 0) {
			int step = 125 / dropShadowsSize;
			// Create new colors
			for (int i = dropShadowsSize - 1; i >= 0; i--) {
				int value = 255 - i * step;
				Color c = new Color(Display.getDefault(), value, value, value);
				dropShadowsColors.add(c);
			}
		}
	}

	private void freeDropShadowsColors() {
		// Free colors :
		{
			Iterator i = this.dropShadowsColors.iterator();
			while (i.hasNext()) {
				Color c = (Color) i.next();
				if (c != null && !c.isDisposed())
					c.dispose();
			}
		}
	}

	public boolean isDropShadows() {
		return dropShadows;
	}

	public void setDropShadows(boolean dropShadows) {
		this.dropShadows = dropShadows;
	}

	public int getDropShadowsSize() {
		return dropShadowsSize;
	}

	public void dispose() {
		freeDropShadowsColors();
	}

	protected Point getBestSize(int originalX, int originalY, int maxX, int maxY) {
		double widthRatio = (double) originalX / (double) maxX;
		double heightRatio = (double) originalY / (double) maxY;

		double bestRatio = widthRatio > heightRatio ? widthRatio : heightRatio;

		int newWidth = (int) ((double) originalX / bestRatio);
		int newHeight = (int) ((double) originalY / bestRatio);

		return new Point(newWidth, newHeight);
	}

	public Color getSelectionBackgroundColor() {
		return selectionBackgroundColor;
	}

	public void setSelectionBackgroundColor(Color selectionColor) {
		this.selectionBackgroundColor = selectionColor;
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Color getDescriptionColor() {
		return descriptionColor;
	}

	public void setDescriptionColor(Color descriptionColor) {
		this.descriptionColor = descriptionColor;
	}

	public Color getSelectionForegroundColor() {
		return selectionForegroundColor;
	}

	public void setSelectionForegroundColor(Color selectionForegroundColor) {
		this.selectionForegroundColor = selectionForegroundColor;
	}
}
