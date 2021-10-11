package com.github.czyzby.setup.data.templates

import com.github.czyzby.setup.config.LibGdxVersion
import com.github.czyzby.setup.data.files.SourceFile
import com.github.czyzby.setup.data.files.path
import com.github.czyzby.setup.data.platforms.*
import com.github.czyzby.setup.data.project.Project

/**
 * Interface shared by all project templates. Templates should be annotated with ProjectTemplate.
 * @author MJ
 */
interface Template {
	val id: String
	// Sizes are kept as strings so you can set the sizes to static values, for example: MainClass.WIDTH.
	val width: String
		get() = "640"
	val height: String
		get() = "480"
	/**
	 * Used as project description in README file. Optional.
	 */
	val description: String
		get() = ""

	/**
	 * @param project is being created. Should contain sources provided by this template.
	 */
	fun apply(project: Project) {
		addApplicationListener(project)
		addAndroidLauncher(project)
		addDesktopLauncher(project)
		addGwtLauncher(project)
		addHeadlessLauncher(project)
		addIOSLauncher(project)
		addLwjgl3Launcher(project)
		addServerLauncher(project)
		project.readmeDescription = description
	}


	fun addApplicationListener(project: Project) {
		addSourceFile(project = project, platform = Core.ID, packageName = project.basic.rootPackage,
				fileName = "${project.basic.mainClass}.java", content = getApplicationListenerContent(project));
	}

	/**
	 * @param project is being created.
	 * @return content of Java class implementing ApplicationListener.
	 */
	fun getApplicationListenerContent(project: Project): String

	fun addDesktopLauncher(project: Project) {
		addSourceFile(project = project, platform = Desktop.ID, packageName = "${project.basic.rootPackage}.desktop",
				fileName = "DesktopLauncher.java", content = getDesktopLauncherContent(project));
	}

	fun getDesktopLauncherContent(project: Project): String = """package ${project.basic.rootPackage}.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ${project.basic.rootPackage}.${project.basic.mainClass};

/** Launches the desktop (LWJGL) application. */
public class DesktopLauncher {
	public static void main(String[] args) {
		createApplication();
	}

	private static LwjglApplication createApplication() {
		return new LwjglApplication(new ${project.basic.mainClass}(), getDefaultConfiguration());
	}

	private static LwjglApplicationConfiguration getDefaultConfiguration() {
		LwjglApplicationConfiguration configuration = new LwjglApplicationConfiguration();
		configuration.title = "${project.basic.name}";
		configuration.width = ${width};
		configuration.height = ${height};
		//// This prevents a confusing error that would appear after exiting normally.
		configuration.forceExit = false;
		
		for (int size : new int[] { 128, 64, 32, 16 }) {
			configuration.addIcon("libgdx" + size + ".png", FileType.Internal);
		}
		return configuration;
	}
}"""

	fun addGwtLauncher(project: Project) {
		addSourceFile(project = project, platform = GWT.ID, packageName = "${project.basic.rootPackage}.gwt",
				fileName = "GwtLauncher.java", content = getGwtLauncherContent(project));
	}

	fun getGwtLauncherContent(project: Project): String = """package ${project.basic.rootPackage}.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import ${project.basic.rootPackage}.${project.basic.mainClass};

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {""" + (if(LibGdxVersion.parseLibGdxVersion(project.advanced.gdxVersion) != null && LibGdxVersion.parseLibGdxVersion(project.advanced.gdxVersion)!! < LibGdxVersion(1, 9, 12))
"""
		////USE THIS CODE FOR A FIXED SIZE APPLICATION
		@Override
		public GwtApplicationConfiguration getConfig () {
				return new GwtApplicationConfiguration($width, $height);
		}
		////END CODE FOR FIXED SIZE APPLICATION

		////UNCOMMENT THIS CODE FOR A RESIZABLE APPLICATION
		//	PADDING is to avoid scrolling in iframes, set to 20 if you have problems
		//	private static final int PADDING = 0;
		//	
		//	@Override
		//	public GwtApplicationConfiguration getConfig() {
		//		int w = Window.getClientWidth() - PADDING;
		//		int h = Window.getClientHeight() - PADDING;
		//		GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(w, h);
		//		Window.enableScrolling(false);
		//		Window.setMargin("0");
		//		Window.addResizeHandler(new ResizeListener());
		//		cfg.preferFlash = false;
		//		return cfg;
		//	}
		//	
		//	class ResizeListener implements ResizeHandler {
		//		@Override
		//		public void onResize(ResizeEvent event) {
        //          if (Gdx.graphics.isFullscreen()) return;
		//			int width = event.getWidth() - PADDING;
		//			int height = event.getHeight() - PADDING;
		//			getRootPanel().setWidth("" + width + "px");
		//			getRootPanel().setHeight("" + height + "px");
		//			getApplicationListener().resize(width, height);
		//			Gdx.graphics.setWindowedMode(width, height);
		//		}
		//	}
		////END OF CODE FOR RESIZABLE APPLICATION
"""
else """
		@Override
		public GwtApplicationConfiguration getConfig () {
			// Resizable application, uses available space in browser with no padding:
			GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(true);
			cfg.padVertical = 0;
			cfg.padHorizontal = 0;
			return cfg;
			// If you want a fixed size application, comment out the above resizable section,
			// and uncomment below:
			//return new GwtApplicationConfiguration($width, $height);
		}
""") + 
"""
		@Override
		public ApplicationListener createApplicationListener () { 
			return new ${project.basic.mainClass}();
		}
}
"""

	fun addAndroidLauncher(project: Project) {
		addSourceFile(project = project, platform = Android.ID, packageName = project.basic.rootPackage,
				fileName = "AndroidLauncher.java", content = getAndroidLauncherContent(project));
	}

	fun getAndroidLauncherContent(project: Project): String = """package ${project.basic.rootPackage};

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import ${project.basic.rootPackage}.${project.basic.mainClass};

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
		initialize(new ${project.basic.mainClass}(), configuration);
	}
}"""

	fun addHeadlessLauncher(project: Project) {
		addSourceFile(project = project, platform = Headless.ID, packageName = "${project.basic.rootPackage}.headless",
				fileName = "HeadlessLauncher.java", content = getHeadlessLauncherContent(project));
	}

	fun getHeadlessLauncherContent(project: Project): String = """package ${project.basic.rootPackage}.headless;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import ${project.basic.rootPackage}.${project.basic.mainClass};

/** Launches the headless application. Can be converted into a utilities project or a server application. */
public class HeadlessLauncher {
	public static void main(String[] args) {
		createApplication();
	}

	private static Application createApplication() {
		// Note: you can use a custom ApplicationListener implementation for the headless project instead of ${project.basic.mainClass}.
		return new HeadlessApplication(new ${project.basic.mainClass}(), getDefaultConfiguration());
	}

	private static HeadlessApplicationConfiguration getDefaultConfiguration() {
		HeadlessApplicationConfiguration configuration = new HeadlessApplicationConfiguration();
		configuration.updatesPerSecond = -1; // When this value is negative, ${project.basic.mainClass}#render() is never called.
		//// If the above line doesn't compile, it is probably because the project libGDX version is older.
		//// In that case, uncomment and use the below line.
		//configuration.renderInterval = -1f; // When this value is negative, ${project.basic.mainClass}#render() is never called.
		return configuration;
	}
}"""

	fun addIOSLauncher(project: Project) {
		addSourceFile(project = project, platform = iOS.ID, packageName = "${project.basic.rootPackage}.ios",
				fileName = "IOSLauncher.java", content = getIOSLauncherContent(project));
	}

	fun getIOSLauncherContent(project: Project): String = """package ${project.basic.rootPackage}.ios;

import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import ${project.basic.rootPackage}.${project.basic.mainClass};

/** Launches the iOS (RoboVM) application. */
public class IOSLauncher extends IOSApplication.Delegate {
	@Override
	protected IOSApplication createApplication() {
		IOSApplicationConfiguration configuration = new IOSApplicationConfiguration();
		return new IOSApplication(new ${project.basic.mainClass}(), configuration);
	}

	public static void main(String[] argv) {
		NSAutoreleasePool pool = new NSAutoreleasePool();
		UIApplication.main(argv, null, IOSLauncher.class);
		pool.close();
	}
}"""


	fun addLwjgl3Launcher(project: Project) {
		addSourceFile(project = project, platform = LWJGL3.ID, packageName = "${project.basic.rootPackage}.lwjgl3",
				fileName = "Lwjgl3Launcher.java", content = getLwjgl3LauncherContent(project));
	}

	fun getLwjgl3LauncherContent(project: Project): String = """package ${project.basic.rootPackage}.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import ${project.basic.rootPackage}.${project.basic.mainClass};

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
	public static void main(String[] args) {
		createApplication();
	}

	private static Lwjgl3Application createApplication() {
		return new Lwjgl3Application(new ${project.basic.mainClass}(), getDefaultConfiguration());
	}

	private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
		Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
		configuration.setTitle("${project.basic.name}");
		configuration.useVsync(true);
		//// Limits FPS to the refresh rate of the currently active monitor.
		configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
		//// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
		//// useful for testing performance, but can also be very stressful to some hardware.
		//// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.
		configuration.setWindowedMode(${width}, ${height});
		configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
		return configuration;
	}
}"""

	fun addServerLauncher(project: Project) {
		addSourceFile(project = project, platform = Server.ID, packageName = "${project.basic.rootPackage}.server",
				fileName = "ServerLauncher.java", content = getServerLauncherContent(project));
	}

	fun getServerLauncherContent(project: Project): String = """package ${project.basic.rootPackage}.server;

/** Launches the server application. */
public class ServerLauncher {
	public static void main(String[] args) {
		// TODO Implement server application.
	}
}"""

	fun addSourceFile(project: Project, platform: String, packageName: String, fileName: String,
					  content: String, sourceFolderPath: String = path("src", "main", "java")) {
		if (project.hasPlatform(platform)) {
			project.files.add(SourceFile(projectName = platform, sourceFolderPath = sourceFolderPath,
					packageName = packageName, fileName = fileName, content = content))
		}
	}
}
