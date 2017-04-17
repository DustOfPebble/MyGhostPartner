package lib.smartwatch;

import com.getpebble.android.kit.util.PebbleDictionary;

public class SmartwatchBundle extends PebbleDictionary {

    public void update(int key, int Value, Boolean Signed){
        if (this.contains(key)) this.remove(key);
        if (Signed) this.addInt32(key,Value);
        else this.addUint32(key,Value);
    }

    public void update(int key, short Value, Boolean Signed){
        if (this.contains(key)) this.remove(key);
        if (Signed) this.addInt16(key,Value);
        else this.addUint16(key,Value);
    }

    public void update(int key, byte Value, Boolean Signed ){
        if (this.contains(key)) this.remove(key);
        if (Signed) this.addInt8(key,Value);
        else this.addUint8(key,Value);
    }

    public void update(int key, String Value ){
        if (this.contains(key)) this.remove(key);
        this.addString(key,Value);
    }
}
