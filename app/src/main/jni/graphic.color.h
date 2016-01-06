//
// Copy the java code from android.graphics.Color.java for getting ARGB Colors
//

int alpha (int color) {
    return (color >> 24) & 0xFF;
}

int red (int color) {
    return (color >> 16) & 0xFF;
}

int green(int color) {
    return (color >> 8) & 0xFF;
}

int blue (int color) {
    return color & 0xFF;
}

int argb(int alpha, int red, int green, int blue) {
    return (alpha << 24) | (red << 16) | (green << 8) | blue;
}