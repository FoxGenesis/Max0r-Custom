module watamebot.max0rcustom {
	exports net.foxgenesis.max0r;

	requires transitive watamebot;

	requires static org.jetbrains.annotations;

	requires okhttp3;
	requires com.fasterxml.jackson.databind;

	exports net.foxgenesis.cats.bean to com.fasterxml.jackson.databind;

	provides net.foxgenesis.watame.plugin.Plugin with net.foxgenesis.max0r.Max0rCustomPlugin;

}