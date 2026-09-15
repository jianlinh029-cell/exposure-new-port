package io.github.mortuusars.exposure.util;

public class Rect2i {
	private final int x;
	private final int y;
	private final int width;
	private final int height;

	public Rect2i(int xPos, int yPos, int width, int height) {
		this.x = xPos;
		this.y = yPos;
		this.width = width;
		this.height = height;
	}

	public Rect2i intersect(Rect2i other) {
        int k = this.x + this.width;
		int l = this.y + this.height;
		int m = other.getX();
		int n = other.getY();
		int o = m + other.getWidth();
		int p = n + other.getHeight();

		return new Rect2i(
				Math.max(this.x, m),
				Math.max(this.y, n),
				Math.max(0, Math.min(k, o) - this.x),
				Math.max(0, Math.min(l, p) - this.y));
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public boolean contains(int x, int y) {
		return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
	}

	@Override
	public String toString() {
		return "{x=" + x +
				", y=" + y +
				", width=" + width +
				", height=" + height +
				'}';
	}
}