module AbstractGameEngineV1 {
	requires java.desktop;
	requires jdk.unsupported.desktop;

	exports com.engine;
	exports com.engine.view;
	exports com.engine.map;
	exports com.engine.event;
	exports com.engine.entity;
	exports com.engine.data;
	exports com.engine.animation;
}