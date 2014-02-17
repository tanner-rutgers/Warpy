#pragma version(1)
#pragma rs java_package_name(ca.tannerrutgers.Warpy.models)

#define PI 3.141592653589793238462643383279502884197169399375

// Input and output variables
const uchar4* input;
uchar4* output;

// Width and height of image
int image_width;
int image_height;

// Calling process can cancel warping
bool is_cancelled;

// Helper method to retrieve pixel from coordinates
static uchar4 getPixelAt(int x, int y) {
    if (y >= image_height) {
        y = image_height - 1;
    }
    if (y < 0) {
        y = 0;
    }
    if (x >= image_width) {
        x = image_width - 1;
    }
    if (x < 0) {
        x = 0;
    }

    return input[y*image_width + x];
}

// Helper method to set pixel at coordinates
void setPixelAt(int x, int y, uchar4 pixel) {
    if (x > 0 && x < image_width && y > 0 && y < image_height) {
        output[y*image_width + x] = pixel;
    }
}

// Applies a bulge warp to the image
void bulge_warp() {

    float center_x = image_width / 2;
    float center_y = image_height / 2;

    float radius = min(center_x, center_y);
    float radius2 = radius*radius;
    float amount = 1.0f;
    float angle = 0;

    int x, y;
    for (y = 0; y < image_height; y++) {
        for (x = 0; x < image_width; x++) {
            float dx = x-center_x;
            float dy = y-center_y;
            float distance = dx*dx + dy*dy;

            if ( distance > radius2 || distance == 0 ) {
                setPixelAt(x,y,getPixelAt(x,y));
            } else {
                float d = (float)sqrt( distance / radius2 );
                float t = (float)pow( sin( (float)(PI*0.5 * d) ), amount);

                dx *= t;
                dy *= t;

                float e = 1 - d;
                float a = angle * e * e;

                float s = (float)sin( a );
                float c = (float)cos( a );

                setPixelAt(x,y,getPixelAt(center_x + c*dx - s*dy, center_y + s*dx + c*dy));
            }
        }
    }
}

// Applies a water ripple warp to the image
void ripple_warp() {

    float ripple_center_x = image_width / 2;
    float ripple_center_y = image_height / 2;

    float radius = min(ripple_center_x, ripple_center_y);
    float radius2 = radius*radius;
    float wavelength = radius / 5;
    float amplitude = 5;

    int x, y;
    for (y = 0; y < image_height; y++) {
        for (x = 0; x < image_width; x++) {

            float dx = x - ripple_center_x;
            float dy = y - ripple_center_y;
            float distance2 = dx*dx + dy*dy;

            if (distance2 > radius2) {
                setPixelAt(x,y,getPixelAt(x,y));
            } else {
                float distance = (float)sqrt(distance2);
                float amount = amplitude * (float)sin((float)(distance / wavelength * PI*2.0f));
                amount *= (radius-distance)/radius;
                if ( distance != 0 )
                    amount *= wavelength/distance;
                setPixelAt(x, y, getPixelAt(x + dx*amount, y + dy*amount));
            }

            // Stop warping if process is cancelled
            if (is_cancelled) {
                return;
            }
        }
    }
}

// Helper function calculating mod of two floats
static float mod(float a, float b) {
    int n = (int)(a/b);

    a -= n*b;
    if (a < 0)
        return a + b;
    return a;
}

// Helper function returning a repeating triangle shape in the range 0..1 with wavelength 1.0
static float triangle(float x) {
    float r = mod(x,1.0f);
    if (r < 0.5) {
        r = 2.0f*r;
    } else {
        r = 2.0f*(1-r);
    }
    return r;
}

// Applies a kaleidoscope warp to the image
void kaleidoscope_warp() {

    float center_x = image_width / 2;
    float center_y = image_height / 2;
    float angle = 45;
    float angle2 = 60;
    float sides = 5;
    float radius = min(center_x, center_y);

    int x, y;
    for (y = 0; y < image_height; y++) {
        for (x = 0; x < image_width; x++) {
            float dx = x-center_x;
            float dy = y-center_y;
            float r = sqrt( dx*dx + dy*dy );
            float theta = atan2( dy, dx ) - angle - angle2;
            theta = triangle((float)( theta/PI*sides*0.5 ));
            if ( radius != 0 ) {
                float c = cos(theta);
                float radiusc = radius/c;
                r = radiusc * triangle( (float)(r/radiusc) );
            }
            theta += angle;

            //setPixelAt(center_x + r*cos(theta), center_y + r*sin(theta), getPixelAt(x,y));
            setPixelAt(x,y,getPixelAt(center_x + r*cos(theta), center_y + r*sin(theta)));
        }
    }
}

