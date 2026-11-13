import 'dart:io';
import 'package:nayom_windows/vault.dart';
import 'package:test/test.dart';

void main() {
  test('appendEvent writes under events/pc', () async {
    final t = Directory.systemTemp.createTempSync();
    final f = await Vault(t.path).appendEvent('{"t":"x"}');
    expect(f.path.contains('events'), isTrue);
    expect(f.readAsStringSync().trim(), '{"t":"x"}');
  });
}
