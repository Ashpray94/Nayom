import 'dart:io';
import 'package:nayom_windows/vault.dart';

Future<void> main(List<String> args) async {
  if (args.isEmpty) {
    stderr.writeln('usage: nayom_windows <vault-path>');
    exit(64);
  }
  final v = Vault(args.first);
  await v.appendEvent('{"t":"start","dev":"pc"}');
  stdout.writeln('Nayom PC online. Watching ${args.first}');
  // TODO: file watcher -> brain (local model) -> proposals/. Extension bridge on localhost only.
}
