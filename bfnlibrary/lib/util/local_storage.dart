
import 'package:crypted_preferences/crypted_preferences.dart';

class Prefs {
  static const PATH = '/prefs';
  static void setDemoBoolean(String isDemo) async {
    final preferences = await Preferences.preferences(path: PATH);
    await preferences.setString('boolKey', isDemo);
    print('🍏 🍏 demo switch set to: $isDemo 🍎 🍎 ');
  }
  static Future<String> getDemoBoolean() async {
    final preferences = await Preferences.preferences(path: PATH);
    var b = preferences.getString('boolKey');
    if (b == null) {
      return null;
    } else {
      print('🍏 🍏 demo switch retrieved: $b 🍏 🍏 ');
      return b;
    }
  }
}
