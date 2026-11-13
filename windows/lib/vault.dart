import 'dart:io';
import 'package:path/path.dart' as p;

/// Vault = the only database. Layout per spec 09. Device code for this machine: pc.
class Vault {
  Vault(this.root);
  final String root;
  static const device = 'pc';

  Directory dir(String name) => Directory(p.join(root, 'Nayom', name));

  /// Files Nayom wrote are hers to change. Existing notes are never edited without review.
  Future<File> writeProposal(String name, String body) async {
    final d = dir('proposals')..createSync(recursive: true);
    return File(p.join(d.path, name)).writeAsString(body);
  }

  /// Each device appends only to its own ledger/events folder, so syncs never conflict.
  Future<File> appendEvent(String line) async {
    final d = dir('events/$device')..createSync(recursive: true);
    final day = DateTime.now().toIso8601String().substring(0, 10);
    final f = File(p.join(d.path, '$day.jsonl'));
    return f.writeAsString('$line\n', mode: FileMode.append);
  }
}
