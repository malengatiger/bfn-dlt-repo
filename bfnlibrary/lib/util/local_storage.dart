
import 'package:crypted_preferences/crypted_preferences.dart';

class Prefs {
  static const PATH = '/prefs';
  static void setDemoString(String isDemo) async {
    final preferences = await Preferences.preferences(path: PATH);
    await preferences.setString('boolKey', isDemo);
    print('🔵 🔵 🔵 demo string set to: $isDemo 🍎 🍎 ');
  }
  static Future<String> getDemoString() async {
    final preferences = await Preferences.preferences(path: PATH);
    var b = preferences.getString('boolKey');
    if (b == null) {
      return null;
    } else {
      print('🔵 🔵 🔵  demo string retrieved: $b 🍏 🍏 ');
      return b;
    }
  }
}
