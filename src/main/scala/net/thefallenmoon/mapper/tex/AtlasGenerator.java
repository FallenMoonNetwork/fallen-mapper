package net.thefallenmoon.mapper.tex;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

//Modified from https://github.com/lukaszdk/texture-atlas-generator/blob/master/AtlasGenerator.java (public domain)
public class AtlasGenerator {
    public static class TextureFile {
        public final String name;
        public final BufferedImage img;

        public TextureFile(String name, BufferedImage img) {
            this.name = name;
            this.img = img;
        }
    }

    public Texture run(int width, int height, int padding, List<TextureFile> imageFiles) throws IOException {
        Set<ImageName> imageNameSet = new TreeSet<>(new ImageNameComparator());

        for (TextureFile f : imageFiles) {
            BufferedImage image = f.img;

            if (image.getWidth() > width || image.getHeight() > height) {
                throw new IOException("Error: '" + f.name + "' (" + image.getWidth() + "x" + image.getHeight() + ") is larger than the atlas (" + width + "x" + height + ")");
            }

            imageNameSet.add(new ImageName(image, f.name));
        }

        Texture texture = new Texture(width, height);

        int count = 0;

        for (ImageName imageName : imageNameSet) {
            if (!texture.addImage(imageName.image, imageName.name, padding)) {
                throw new IOException("Too small of an atlas!");
            }
        }

        return texture;
    }

    private class ImageName {
        public BufferedImage image;
        public String name;

        public ImageName(BufferedImage image, String name) {
            this.image = image;
            this.name = name;
        }
    }

    private class ImageNameComparator implements Comparator<ImageName> {
        public int compare(ImageName image1, ImageName image2) {
            int h1 = image1.image.getHeight();
            int h2 = image2.image.getHeight();

            if (h1 != h2) {
                return h2 - h1;
            }

            int w1 = image1.image.getWidth();
            int w2 = image2.image.getWidth();

            if (w1 != w2) {
                return w2 - w1;
            }

            int area1 = w1 * h1;
            int area2 = w2 * h2;

            if (area1 != area2) {
                return area2 - area1;
            } else {
                return image1.name.compareTo(image2.name);
            }
        }
    }

    public class Texture {
        private class Node {
            public Rectangle rect;
            public Node child[];
            public BufferedImage image;

            public Node(int x, int y, int width, int height) {
                rect = new Rectangle(x, y, width, height);
                child = new Node[2];
                child[0] = null;
                child[1] = null;
                image = null;
            }

            public boolean isLeaf() {
                return child[0] == null && child[1] == null;
            }

            // Algorithm from http://www.blackpawn.com/texts/lightmaps/
            public Node insert(BufferedImage image, int padding) {
                if (!isLeaf()) {
                    Node newNode = child[0].insert(image, padding);

                    if (newNode != null) {
                        return newNode;
                    }

                    return child[1].insert(image, padding);
                } else {
                    if (this.image != null) {
                        return null; // occupied
                    }

                    if (image.getWidth() > rect.width || image.getHeight() > rect.height) {
                        return null; // does not fit
                    }

                    if (image.getWidth() == rect.width && image.getHeight() == rect.height) {
                        this.image = image; // perfect fit
                        return this;
                    }

                    int dw = rect.width - image.getWidth();
                    int dh = rect.height - image.getHeight();

                    if (dw > dh) {
                        child[0] = new Node(rect.x, rect.y, image.getWidth(), rect.height);
                        child[1] = new Node(padding + rect.x + image.getWidth(), rect.y, rect.width - image.getWidth() - padding, rect.height);
                    } else {
                        child[0] = new Node(rect.x, rect.y, rect.width, image.getHeight());
                        child[1] = new Node(rect.x, padding + rect.y + image.getHeight(), rect.width, rect.height - image.getHeight() - padding);
                    }
                    /*if(dw > dh)
                    {
						child[0] = new Node(rect.x, rect.y, image.getWidth(), rect.height);
						child[1] = new Node(padding+rect.x+image.getWidth(), rect.y, rect.width - image.getWidth(), rect.height);
					}
					else
					{
						child[0] = new Node(rect.x, rect.y, rect.width, image.getHeight());
						child[1] = new Node(rect.x, padding+rect.y+image.getHeight(), rect.width, rect.height - image.getHeight());
					}*/

                    return child[0].insert(image, padding);
                }
            }
        }

        private BufferedImage image;
        private Graphics2D graphics;
        private Node root;
        private Map<String, Rectangle> rectangleMap;

        public Texture(int width, int height) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            graphics = image.createGraphics();

            root = new Node(0, 0, width, height);
            rectangleMap = new TreeMap<String, Rectangle>();
        }

        public boolean addImage(BufferedImage image, String name, int padding) {
            Node node = root.insert(image, padding);

            if (node == null) {
                return false;
            }

            rectangleMap.put(name, node.rect);
            graphics.drawImage(image, null, node.rect.x, node.rect.y);


            return true;
        }

        public BufferedImage getImage() {
            return image;
        }

        public Map<String, Rectangle> getRectangleMap() {
            return rectangleMap;
        }
    }
}