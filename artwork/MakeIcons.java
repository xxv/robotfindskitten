import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Generates the adaptive launcher icon layers from rfk.png.
 *
 * The crop matches the legacy icon (see icon.svg): the visible 72dp area of the adaptive icon
 * shows exactly what the old square icon showed, with the rest of the 108dp layer filled in from
 * the surrounding artwork.
 *
 * Run from the project root with: java artwork/MakeIcons.java
 */
public class MakeIcons {
    // Background color of rfk.png; keep in sync with res/values/ic_launcher.xml
    static final int BACKGROUND = 0x002368;

    // Placement of rfk.png in icon.svg, in SVG units, and of the clip rectangle it's cropped to.
    static final double SVG_IMAGE_X = -28.591833, SVG_IMAGE_Y = 974.1748,
            SVG_IMAGE_WIDTH = 131.98305;
    static final double SVG_CLIP_X = 3.7837861, SVG_CLIP_Y = 984.146, SVG_CLIP_SIZE = 64.432426;

    static final String[] DENSITIES = { "mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi" };
    static final double[] SCALES = { 1, 1.5, 2, 3, 4 };

    public static void main(String[] args) throws IOException {
        final BufferedImage src = ImageIO.read(new File("artwork/rfk.png"));
        final int w = src.getWidth(), h = src.getHeight();

        // Flatten onto the background, as the edges of rfk.png are slightly transparent.
        final double[][] color = new double[w * h][];
        final double[][] mono = new double[w * h][];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int argb = src.getRGB(x, y);
                final double a = (argb >>> 24) / 255.0;
                final double r = blend(argb >> 16, BACKGROUND >> 16, a);
                final double g = blend(argb >> 8, BACKGROUND >> 8, a);
                final double b = blend(argb, BACKGROUND, a);
                color[y * w + x] = new double[] { 1, r, g, b };

                // Monochrome: the light parts of the drawing are opaque, while the dark outlines
                // and the background are transparent, so the line work shows as cut-outs.
                final double luma = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
                final double alpha = clamp((luma - 0.18) / 0.12);
                mono[y * w + x] = new double[] { alpha, 255, 255, 255 };
            }
        }

        final double svgScale = SVG_IMAGE_WIDTH / w;
        final double cropSize = SVG_CLIP_SIZE / svgScale;
        final double centerX = (SVG_CLIP_X - SVG_IMAGE_X) / svgScale + cropSize / 2;
        final double centerY = (SVG_CLIP_Y - SVG_IMAGE_Y) / svgScale + cropSize / 2;
        // The legacy crop fills the 72dp visible area of the 108dp adaptive icon layer.
        final double layerSize = cropSize * 108 / 72;

        final double[] background = { 1, BACKGROUND >> 16 & 0xff, BACKGROUND >> 8 & 0xff,
                BACKGROUND & 0xff };
        final double[] transparent = { 0, 255, 255, 255 };

        for (int i = 0; i < DENSITIES.length; i++) {
            final int size = (int) Math.round(108 * SCALES[i]);
            final File dir = new File("app/src/main/res/mipmap-" + DENSITIES[i]);
            dir.mkdirs();
            // The layer extends past the top of the artwork, so fill that in with the background.
            write(resample(color, w, h, centerX, centerY, layerSize, size, background),
                    new File(dir, "ic_launcher_foreground.png"));
            write(resample(mono, w, h, centerX, centerY, layerSize, size, transparent),
                    new File(dir, "ic_launcher_monochrome.png"));
        }
    }

    static double blend(int channel, int background, double alpha) {
        return (channel & 0xff) * alpha + (background & 0xff) * (1 - alpha);
    }

    static double clamp(double v) {
        return Math.max(0, Math.min(1, v));
    }

    /**
     * Resamples a square region of a premultiplied image into a size x size image by averaging a
     * grid of bilinear samples per output pixel. Anything outside the source is treated as the
     * outside pixel.
     */
    static BufferedImage resample(double[][] px, int w, int h, double centerX, double centerY,
            double regionSize, int size, double[] outside) {
        final BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        final double step = regionSize / size;
        final int samples = Math.max(2, (int) Math.ceil(step * 2));
        final double left = centerX - regionSize / 2, top = centerY - regionSize / 2;

        for (int oy = 0; oy < size; oy++) {
            for (int ox = 0; ox < size; ox++) {
                final double[] sum = new double[4];
                for (int sy = 0; sy < samples; sy++) {
                    for (int sx = 0; sx < samples; sx++) {
                        final double x = left + (ox + (sx + 0.5) / samples) * step - 0.5;
                        final double y = top + (oy + (sy + 0.5) / samples) * step - 0.5;
                        bilinear(px, w, h, x, y, outside, sum);
                    }
                }
                final double n = samples * samples;
                final double a = sum[0] / n;
                int argb = 0;
                if (a > 0) {
                    argb = (int) Math.round(a * 255) << 24
                            | channel(sum[1] / n / a) << 16
                            | channel(sum[2] / n / a) << 8
                            | channel(sum[3] / n / a);
                }
                out.setRGB(ox, oy, argb);
            }
        }
        return out;
    }

    /** Adds the premultiplied bilinear sample at (x, y) to sum. */
    static void bilinear(double[][] px, int w, int h, double x, double y, double[] outside,
            double[] sum) {
        final int x0 = (int) Math.floor(x), y0 = (int) Math.floor(y);
        final double fx = x - x0, fy = y - y0;
        for (int dy = 0; dy <= 1; dy++) {
            for (int dx = 0; dx <= 1; dx++) {
                final int xi = x0 + dx, yi = y0 + dy;
                final boolean inside = xi >= 0 && yi >= 0 && xi < w && yi < h;
                final double weight = (dx == 0 ? 1 - fx : fx) * (dy == 0 ? 1 - fy : fy);
                final double[] p = inside ? px[yi * w + xi] : outside;
                sum[0] += p[0] * weight;
                for (int c = 1; c < 4; c++) {
                    sum[c] += p[0] * p[c] * weight;
                }
            }
        }
    }

    static int channel(double v) {
        return (int) Math.round(Math.max(0, Math.min(255, v)));
    }

    static void write(BufferedImage image, File file) throws IOException {
        ImageIO.write(image, "png", file);
        System.out.println("wrote " + file);
    }
}
