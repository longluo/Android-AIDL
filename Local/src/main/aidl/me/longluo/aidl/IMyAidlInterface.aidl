package me.longluo.aidl;

interface IMyAidlInterface {
    /**
    * Request that a line be drawn between the
    * two given coordinates.
    */
    void drawLine(int x1, int y1, int x2, int y2);
}